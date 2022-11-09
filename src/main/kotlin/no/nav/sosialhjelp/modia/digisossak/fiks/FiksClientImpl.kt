package no.nav.sosialhjelp.modia.digisossak.fiks

import no.finn.unleash.Unleash
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.exceptions.FiksClientException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksNotFoundException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksServerException
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.app.client.RetryUtils.retryBackoffSpec
import no.nav.sosialhjelp.modia.app.maskinporten.MaskinportenClient
import no.nav.sosialhjelp.modia.client.unleash.FIKS_CACHE_ENABLED
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksPaths.PATH_ALLE_DIGISOSSAKER
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksPaths.PATH_DIGISOSSAK
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksPaths.PATH_DOKUMENT
import no.nav.sosialhjelp.modia.kommune.KommuneService
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.logging.AuditService
import no.nav.sosialhjelp.modia.maskerFnr
import no.nav.sosialhjelp.modia.messageUtenFnr
import no.nav.sosialhjelp.modia.redis.RedisKeyType
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.utils.IntegrationUtils
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import no.nav.sosialhjelp.modia.utils.RequestUtils
import no.nav.sosialhjelp.modia.utils.objectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@Component
class FiksClientImpl(
    private val fiksWebClient: WebClient,
    private val clientProperties: ClientProperties,
    private val maskinportenClient: MaskinportenClient,
    private val kommuneService: KommuneService,
    private val auditService: AuditService,
    private val redisService: RedisService,
    private val unleash: Unleash,
    @Value("\${retry_fiks_max_attempts}") private val maxAttempts: Long,
    @Value("\${retry_fiks_initial_delay}") private val initialDelay: Long,
) : FiksClient {

    private val baseUrl = clientProperties.fiksDigisosEndpointUrl

    private val fiksRetry = retryBackoffSpec(maxAttempts = maxAttempts, initialWaitIntervalMillis = initialDelay)
        .onRetryExhaustedThrow { spec, retrySignal ->
            throw FiksServerException(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Fiks - retry har nådd max antall forsøk (=${spec.maxAttempts})",
                retrySignal.failure()
            )
        }

    override fun hentDigisosSak(digisosId: String): DigisosSak {
        return hentDigisosSakFraCache(digisosId)?.also { log.info("Hentet digisosSak=$digisosId fra cache") }
            ?: hentDigisosSakFraFiks(digisosId)
    }

    override fun hentDokument(
        fnr: String,
        digisosId: String,
        dokumentlagerId: String,
        requestedClass: Class<out Any>
    ): Any {
        return hentDokumentFraCache(dokumentlagerId, requestedClass)?.also { log.info("Hentet dokument=$dokumentlagerId fra cache") }
            ?: hentDokumentFraFiks(fnr, digisosId, dokumentlagerId, requestedClass)
    }

    private fun skalBrukeCache(): Boolean {
        return unleash.isEnabled(FIKS_CACHE_ENABLED, false) && RequestUtils.getSosialhjelpModiaSessionId() != null
    }

    private fun hentDigisosSakFraCache(digisosId: String): DigisosSak? {
        if (skalBrukeCache()) {
            log.debug("Forsøker å hente digisosSak fra cache")
            return redisService.get(RedisKeyType.FIKS_CLIENT, cacheKeyFor(digisosId), DigisosSak::class.java) as DigisosSak?
        }
        return null
    }

    // cache key = "<sessionId>_<digisosId>" eller "<sessionId>_<dokumentlagerId>"
    private fun cacheKeyFor(id: String) = "${RequestUtils.getSosialhjelpModiaSessionId()}_$id"

    private fun hentDigisosSakFraFiks(digisosId: String): DigisosSak {
        val sporingsId = genererSporingsId()

        val digisosSak: DigisosSak = fiksWebClient.get()
            .uri(PATH_DIGISOSSAK.plus(sporingsIdQuery), digisosId, sporingsId)
            .accept(MediaType.APPLICATION_JSON)
            .header(IntegrationUtils.HEADER_INTEGRASJON_ID, clientProperties.fiksIntegrasjonId)
            .header(IntegrationUtils.HEADER_INTEGRASJON_PASSORD, clientProperties.fiksIntegrasjonpassord)
            .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .retrieve()
            .bodyToMono<DigisosSak>()
            .retryWhen(fiksRetry)
            .onErrorMap(WebClientResponseException::class.java) { e ->
                log.warn("Fiks - hentDigisosSak feilet - ${messageUtenFnr(e)}", e)
                when {
                    e.statusCode == HttpStatus.NOT_FOUND -> FiksNotFoundException(e.message?.maskerFnr, e)
                    e.statusCode.is4xxClientError -> FiksClientException(e.rawStatusCode, e.message?.maskerFnr, e)
                    else -> FiksServerException(e.rawStatusCode, e.message?.maskerFnr, e)
                }
            }
            .block() ?: throw FiksServerException(500, "Fiks - DigisosSak nedlasting feilet!", null)

        if (!isDigisosSakNewerThanMonths(digisosSak, 15)) {
            throw FiksNotFoundException("Fiks - DigisosSak er for gammel!", null)
        }
        log.info("Hentet DigisosSak $digisosId fra Fiks")
        return digisosSak
            .also {
                auditService.reportFiks(it.sokerFnr, "$baseUrl/digisos/api/v1/nav/soknader/$digisosId", HttpMethod.GET, sporingsId)
                lagreTilCache(digisosId, it)
            }
    }

    private fun lagreTilCache(id: String, any: Any) {
        if (skalBrukeCache()) {
            log.info("Lagret digisossak/dokument id=$id til cache")
            redisService.set(RedisKeyType.FIKS_CLIENT, cacheKeyFor(id), objectMapper.writeValueAsBytes(any))
        }
    }

    private fun hentDokumentFraCache(dokumentlagerId: String, requestedClass: Class<out Any>): Any? {
        if (skalBrukeCache()) {
            log.debug("Forsøker å hente dokument fra cache")
            return redisService.get(RedisKeyType.FIKS_CLIENT, cacheKeyFor(dokumentlagerId), requestedClass)
        }
        return null
    }

    private fun hentDokumentFraFiks(fnr: String, digisosId: String, dokumentlagerId: String, requestedClass: Class<out Any>): Any {
        val sporingsId = genererSporingsId()

        val dokument: Any = fiksWebClient.get()
            .uri(PATH_DOKUMENT.plus(sporingsIdQuery), digisosId, dokumentlagerId, sporingsId)
            .accept(MediaType.APPLICATION_JSON)
            .header(IntegrationUtils.HEADER_INTEGRASJON_ID, clientProperties.fiksIntegrasjonId)
            .header(IntegrationUtils.HEADER_INTEGRASJON_PASSORD, clientProperties.fiksIntegrasjonpassord)
            .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .retrieve()
            .bodyToMono(requestedClass)
            .retryWhen(fiksRetry)
            .onErrorMap(WebClientResponseException::class.java) { e ->
                log.warn("Fiks - hentDokument feilet - ${messageUtenFnr(e)}", e)
                when {
                    e.statusCode.is4xxClientError -> FiksClientException(e.rawStatusCode, e.message?.maskerFnr, e)
                    else -> FiksServerException(e.rawStatusCode, e.message?.maskerFnr, e)
                }
            }
            .block() ?: throw FiksServerException(500, "Fiks - Dokument nedlasting feilet!", null)

        log.info("Hentet dokument (${requestedClass.simpleName}) fra Fiks, dokumentlagerId $dokumentlagerId")
        return dokument
            .also {
                auditService.reportFiks(fnr, "$baseUrl/digisos/api/v1/nav/soknader/$digisosId/dokumenter/$dokumentlagerId", HttpMethod.GET, sporingsId)
                lagreTilCache(dokumentlagerId, it)
            }
    }

    override fun hentAlleDigisosSaker(fnr: String): List<DigisosSak> {
        val sporingsId = genererSporingsId()

        val digisosSaker: List<DigisosSak> = fiksWebClient.post()
            .uri(PATH_ALLE_DIGISOSSAKER.plus(sporingsIdQuery), sporingsId)
            .accept(MediaType.APPLICATION_JSON)
            .header(IntegrationUtils.HEADER_INTEGRASJON_ID, clientProperties.fiksIntegrasjonId)
            .header(IntegrationUtils.HEADER_INTEGRASJON_PASSORD, clientProperties.fiksIntegrasjonpassord)
            .header(HttpHeaders.AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .body(BodyInserters.fromValue(Fnr(fnr)))
            .retrieve()
            .bodyToMono<List<DigisosSak>>()
            .retryWhen(fiksRetry)
            .onErrorMap(WebClientResponseException::class.java) { e ->
                log.warn("Fiks - hentAlleDigisosSaker feilet - ${messageUtenFnr(e)}", e)
                when {
                    e.statusCode.is4xxClientError -> FiksClientException(e.rawStatusCode, e.message?.maskerFnr, e)
                    else -> FiksServerException(e.rawStatusCode, e.message?.maskerFnr, e)
                }
            }
            .block() ?: throw FiksServerException(500, "Fiks - AlleDigisosSaker nedlasting feilet!", null)

        log.info("Hentet ${digisosSaker.size} saker fra Fiks (før filter.)")
        return digisosSaker
            .filter { isDigisosSakNewerThanMonths(it, 15) }
            .filter { kommuneHasActivatedInnsynNKS(it) }
            .also { auditService.reportFiks(fnr, baseUrl + PATH_ALLE_DIGISOSSAKER, HttpMethod.POST, sporingsId) }
    }

    private fun isDigisosSakNewerThanMonths(digisosSak: DigisosSak, months: Int): Boolean =
        digisosSak.sistEndret >= LocalDateTime.now().minusMonths(months.toLong())
            .toInstant(ZoneOffset.UTC).toEpochMilli()

    private fun kommuneHasActivatedInnsynNKS(digisosSak: DigisosSak): Boolean {
        return kommuneService.get(digisosSak.kommunenummer).harNksTilgang
    }

    private fun genererSporingsId(): String = UUID.randomUUID().toString()

    companion object {
        private val log by logger()

        private val sporingsIdQuery: String
            get() = "?$SPORINGSID={$SPORINGSID}"

        //        Query param navn
        private const val SPORINGSID = "sporingsId"
    }

    private data class Fnr(
        val fnr: String
    )
}
