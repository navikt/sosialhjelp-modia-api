package no.nav.sosialhjelp.modia.digisossak.fiks

import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.exceptions.FiksClientException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksNotFoundException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksServerException
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.app.client.RetryUtils.retryBackoffSpecWithConnectionErrors
import no.nav.sosialhjelp.modia.auth.texas.TexasClient
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksPaths.PATH_ALLE_DIGISOSSAKER
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksPaths.PATH_DIGISOSSAK
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksPaths.PATH_DOKUMENT
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.logging.AuditService
import no.nav.sosialhjelp.modia.maskerFnr
import no.nav.sosialhjelp.modia.messageUtenFnr
import no.nav.sosialhjelp.modia.redis.RedisKeyType
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.utils.IntegrationUtils
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import no.nav.sosialhjelp.modia.utils.RequestUtils
import no.nav.sosialhjelp.modia.utils.sosialhjelpJsonMapper
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
    private val auditService: AuditService,
    private val redisService: RedisService,
    @Value("\${retry_fiks_max_attempts}") private val maxAttempts: Long,
    @Value("\${retry_fiks_initial_delay}") private val initialDelay: Long,
    @Value("\${dokument_cache_time_to_live_seconds}") private val documentTTL: Long,
    private val texasClient: TexasClient,
) : FiksClient {
    private val baseUrl = clientProperties.fiksDigisosEndpointUrl

    private val fiksRetry =
        retryBackoffSpecWithConnectionErrors(maxAttempts = maxAttempts, initialWaitIntervalMillis = initialDelay)
            .onRetryExhaustedThrow { spec, retrySignal ->
                throw FiksServerException(
                    HttpStatus.SERVICE_UNAVAILABLE.value(),
                    "Fiks - retry har nådd max antall forsøk (=${spec.maxAttempts})",
                    retrySignal.failure(),
                )
            }

    override suspend fun hentDigisosSak(digisosId: String): DigisosSak =
        hentDigisosSakFraCache(digisosId)?.also { log.info("Hentet digisosSak=$digisosId fra cache") }
            ?: hentDigisosSakFraFiks(digisosId)

    override suspend fun <T : Any> hentDokument(
        fnr: String,
        digisosId: String,
        dokumentlagerId: String,
        requestedClass: Class<out T>,
        cacheKey: String?,
    ): T =
        hentDokumentFraCache(cacheKey ?: dokumentlagerId, requestedClass)?.also { log.info("Hentet dokument=$dokumentlagerId fra cache") }
            ?: hentDokumentFraFiks(fnr, digisosId, dokumentlagerId, requestedClass, cacheKey ?: dokumentlagerId)

    private fun skalBrukeCache(): Boolean = RequestUtils.getSosialhjelpModiaSessionId() != null

    private fun hentDigisosSakFraCache(digisosId: String): DigisosSak? {
        if (skalBrukeCache()) {
            log.debug("Forsøker å hente digisosSak fra cache")
            return redisService.get(RedisKeyType.FIKS_CLIENT, cacheKeyFor(digisosId), DigisosSak::class.java)
        }
        return null
    }

    // cache key = "<sessionId>_<digisosId>" eller "<sessionId>_<dokumentlagerId>"
    private fun cacheKeyFor(id: String) = "${RequestUtils.getSosialhjelpModiaSessionId()}_$id"

    private suspend fun hentDigisosSakFraFiks(digisosId: String): DigisosSak {
        val sporingsId = genererSporingsId()

        val digisosSak: DigisosSak =
            fiksWebClient
                .get()
                .uri(PATH_DIGISOSSAK.plus(sporingsIdQuery), digisosId, sporingsId)
                .accept(MediaType.APPLICATION_JSON)
                .header(IntegrationUtils.HEADER_INTEGRASJON_ID, clientProperties.fiksIntegrasjonId)
                .header(IntegrationUtils.HEADER_INTEGRASJON_PASSORD, clientProperties.fiksIntegrasjonpassord)
                .header(HttpHeaders.AUTHORIZATION, BEARER + texasClient.getMaskinportenToken())
                .retrieve()
                .bodyToMono<DigisosSak>()
                .retryWhen(fiksRetry)
                .onErrorMap(WebClientResponseException::class.java) { e ->
                    log.warn("Fiks - hentDigisosSak feilet - ${messageUtenFnr(e)}", e)
                    when {
                        e.statusCode == HttpStatus.NOT_FOUND -> FiksNotFoundException(e.message.maskerFnr, e)
                        e.statusCode.is4xxClientError ->
                            FiksClientException(
                                e.statusCode.value(),
                                e.message.maskerFnr,
                                e,
                            )
                        else -> FiksServerException(e.statusCode.value(), e.message.maskerFnr, e)
                    }
                }.block() ?: throw FiksServerException(500, "Fiks - DigisosSak nedlasting feilet!", null)

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

    private fun lagreTilCache(
        id: String,
        content: Any,
        timeToLive: Long = redisService.defaultTimeToLiveSeconds,
    ) {
        if (skalBrukeCache()) {
            log.info("Lagret digisossak/dokument id=$id til cache")
            redisService.set(RedisKeyType.FIKS_CLIENT, cacheKeyFor(id), sosialhjelpJsonMapper.writeValueAsBytes(content), timeToLive)
        }
    }

    private fun <T : Any> hentDokumentFraCache(
        cacheKey: String,
        requestedClass: Class<out T>,
    ): T? {
        if (skalBrukeCache()) {
            log.debug("Forsøker å hente dokument fra cache")
            return redisService.get(RedisKeyType.FIKS_CLIENT, cacheKeyFor(cacheKey), requestedClass)
        }
        return null
    }

    private suspend fun <T : Any> hentDokumentFraFiks(
        fnr: String,
        digisosId: String,
        dokumentlagerId: String,
        requestedClass: Class<out T>,
        cacheKey: String,
    ): T {
        val sporingsId = genererSporingsId()

        val dokument =
            fiksWebClient
                .get()
                .uri(PATH_DOKUMENT.plus(sporingsIdQuery), digisosId, dokumentlagerId, sporingsId)
                .accept(MediaType.APPLICATION_JSON)
                .header(IntegrationUtils.HEADER_INTEGRASJON_ID, clientProperties.fiksIntegrasjonId)
                .header(IntegrationUtils.HEADER_INTEGRASJON_PASSORD, clientProperties.fiksIntegrasjonpassord)
                .header(HttpHeaders.AUTHORIZATION, BEARER + texasClient.getMaskinportenToken())
                .retrieve()
                .bodyToMono(requestedClass)
                .retryWhen(fiksRetry)
                .onErrorMap(WebClientResponseException::class.java) { e ->
                    log.warn("Fiks - hentDokument feilet - ${messageUtenFnr(e)}", e)
                    when {
                        e.statusCode.is4xxClientError ->
                            FiksClientException(
                                e.statusCode.value(),
                                e.message.maskerFnr,
                                e,
                            )
                        else -> FiksServerException(e.statusCode.value(), e.message.maskerFnr, e)
                    }
                }.block() ?: throw FiksServerException(500, "Fiks - Dokument nedlasting feilet!", null)

        log.info("Hentet dokument (${requestedClass.simpleName}) fra Fiks, dokumentlagerId $dokumentlagerId")
        return dokument
            .also {
                auditService.reportFiks(
                    fnr,
                    "$baseUrl/digisos/api/v1/nav/soknader/$digisosId/dokumenter/$dokumentlagerId",
                    HttpMethod.GET,
                    sporingsId,
                )
                lagreTilCache(cacheKey, it, documentTTL)
            }
    }

    override suspend fun hentAlleDigisosSaker(fnr: String): List<DigisosSak> {
        val sporingsId = genererSporingsId()

        val digisosSaker: List<DigisosSak> =
            fiksWebClient
                .post()
                .uri(PATH_ALLE_DIGISOSSAKER.plus(sporingsIdQuery), sporingsId)
                .accept(MediaType.APPLICATION_JSON)
                .header(IntegrationUtils.HEADER_INTEGRASJON_ID, clientProperties.fiksIntegrasjonId)
                .header(IntegrationUtils.HEADER_INTEGRASJON_PASSORD, clientProperties.fiksIntegrasjonpassord)
                .header(HttpHeaders.AUTHORIZATION, BEARER + texasClient.getMaskinportenToken())
                .body(BodyInserters.fromValue(Fnr(fnr)))
                .retrieve()
                .bodyToMono<List<DigisosSak>>()
                .retryWhen(fiksRetry)
                .onErrorMap(WebClientResponseException::class.java) { e ->
                    log.warn("Fiks - hentAlleDigisosSaker feilet - ${messageUtenFnr(e)}", e)
                    when {
                        e.statusCode.is4xxClientError ->
                            FiksClientException(
                                e.statusCode.value(),
                                e.message.maskerFnr,
                                e,
                            )
                        else -> FiksServerException(e.statusCode.value(), e.message.maskerFnr, e)
                    }
                }.block() ?: throw FiksServerException(500, "Fiks - AlleDigisosSaker nedlasting feilet!", null)

        log.info("Hentet ${digisosSaker.size} saker fra Fiks (før filter.)")
        return digisosSaker
            .filter { isDigisosSakNewerThanMonths(it, 15) }
            .also { auditService.reportFiks(fnr, baseUrl + PATH_ALLE_DIGISOSSAKER, HttpMethod.POST, sporingsId) }
    }

    fun isDigisosSakNewerThanMonths(
        digisosSak: DigisosSak,
        months: Int,
    ): Boolean =
        digisosSak.sistEndret >=
            LocalDateTime
                .now()
                .minusMonths(months.toLong())
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()

    private fun genererSporingsId(): String = UUID.randomUUID().toString()

    companion object {
        private val log by logger()

        private val sporingsIdQuery: String
            get() = "?$SPORINGSID={$SPORINGSID}"

        //        Query param navn
        private const val SPORINGSID = "sporingsId"
    }

    private data class Fnr(
        val fnr: String,
    )
}
