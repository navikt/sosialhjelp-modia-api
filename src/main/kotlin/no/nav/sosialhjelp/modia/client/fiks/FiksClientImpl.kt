package no.nav.sosialhjelp.modia.client.fiks

import kotlinx.coroutines.runBlocking
import no.finn.unleash.Unleash
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.exceptions.FiksClientException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksNotFoundException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksServerException
import no.nav.sosialhjelp.kotlin.utils.retry
import no.nav.sosialhjelp.modia.client.fiks.FiksPaths.PATH_ALLE_DIGISOSSAKER
import no.nav.sosialhjelp.modia.client.fiks.FiksPaths.PATH_DIGISOSSAK
import no.nav.sosialhjelp.modia.client.fiks.FiksPaths.PATH_DOKUMENT
import no.nav.sosialhjelp.modia.client.maskinporten.MaskinportenClient
import no.nav.sosialhjelp.modia.client.unleash.BERGEN_ENABLED
import no.nav.sosialhjelp.modia.client.unleash.FIKS_CACHE_ENABLED
import no.nav.sosialhjelp.modia.client.unleash.STAVANGER_ENABLED
import no.nav.sosialhjelp.modia.common.ManglendeTilgangException
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.logging.AuditService
import no.nav.sosialhjelp.modia.maskerFnr
import no.nav.sosialhjelp.modia.messageUtenFnr
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.typeRef
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.fiksHeaders
import no.nav.sosialhjelp.modia.utils.RequestUtils
import no.nav.sosialhjelp.modia.utils.objectMapper
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.UUID

@Component
class FiksClientImpl(
    private val fiksWebClient: WebClient,
    private val clientProperties: ClientProperties,
    private val maskinportenClient: MaskinportenClient,
    private val auditService: AuditService,
    private val redisService: RedisService,
    private val unleash: Unleash,
    private val retryProperties: FiksRetryProperties,
) : FiksClient {

    private val baseUrl = clientProperties.fiksDigisosEndpointUrl

    override fun hentDigisosSak(digisosId: String): DigisosSak {
        return hentDigisosSakFraCache(digisosId)?.also { log.info("Hentet digisosSak=$digisosId fra cache") }
            ?: hentDigisosSakFraFiks(digisosId)
    }

    override fun hentDokument(fnr: String, digisosId: String, dokumentlagerId: String, requestedClass: Class<out Any>): Any {
        return hentDokumentFraCache(dokumentlagerId, requestedClass)?.also { log.info("Hentet dokument=$dokumentlagerId fra cache") }
            ?: hentDokumentFraFiks(fnr, digisosId, dokumentlagerId, requestedClass)
    }

    private fun skalBrukeCache(): Boolean {
        return unleash.isEnabled(FIKS_CACHE_ENABLED, false) && RequestUtils.getSosialhjelpModiaSessionId() != null
    }

    private fun hentDigisosSakFraCache(digisosId: String): DigisosSak? {
        if (skalBrukeCache()) {
            log.debug("Forsøker å hente digisosSak fra cache")
            return redisService.get(cacheKeyFor(digisosId), DigisosSak::class.java) as DigisosSak?
        }
        return null
    }

    // cache key = "<sessionId>_<digisosId>" eller "<sessionId>_<dokumentlagerId>"
    private fun cacheKeyFor(id: String) = "${RequestUtils.getSosialhjelpModiaSessionId()}_$id"

    private fun hentDigisosSakFraFiks(digisosId: String): DigisosSak {
        val sporingsId = genererSporingsId()

        val digisosSak: DigisosSak = withRetry {
            fiksWebClient.get()
                .uri(PATH_DIGISOSSAK.plus(sporingsIdQuery), digisosId, sporingsId)
                .headers { it.addAll(fiksHeaders(clientProperties, BEARER + maskinportenClient.getToken())) }
                .retrieve()
                .bodyToMono<DigisosSak>()
                .onErrorMap(WebClientResponseException::class.java) { e ->
                    log.warn("Fiks - hentDigisosSak feilet - ${messageUtenFnr(e)}", e)
                    when {
                        e.statusCode == HttpStatus.NOT_FOUND -> FiksNotFoundException(e.message?.maskerFnr, e)
                        e.statusCode.is4xxClientError -> FiksClientException(e.rawStatusCode, e.message?.maskerFnr, e)
                        else -> FiksServerException(e.rawStatusCode, e.message?.maskerFnr, e)
                    }
                }
                .block() ?: throw FiksServerException(500, "Fiks - DigisosSak nedlasting feilet!", null)
        }

        if (!harKommunenTilgangTilModia(digisosSak.kommunenummer)) {
            throw ManglendeTilgangException("Fiks - DigisosSak tilhører en kommune uten tilgang!")
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
            redisService.set(cacheKeyFor(id), objectMapper.writeValueAsBytes(any))
        }
    }

    private fun hentDokumentFraCache(dokumentlagerId: String, requestedClass: Class<out Any>): Any? {
        if (skalBrukeCache()) {
            log.debug("Forsøker å hente dokument fra cache")
            return redisService.get(cacheKeyFor(dokumentlagerId), requestedClass)
        }
        return null
    }

    private fun hentDokumentFraFiks(fnr: String, digisosId: String, dokumentlagerId: String, requestedClass: Class<out Any>): Any {
        val sporingsId = genererSporingsId()

        val dokument: Any = withRetry {
            fiksWebClient.get()
                .uri(PATH_DOKUMENT.plus(sporingsIdQuery), digisosId, dokumentlagerId, sporingsId)
                .headers { it.addAll(fiksHeaders(clientProperties, BEARER + maskinportenClient.getToken())) }
                .retrieve()
                .bodyToMono(requestedClass)
                .onErrorMap(WebClientResponseException::class.java) { e ->
                    log.warn("Fiks - hentDokument feilet - ${messageUtenFnr(e)}", e)
                    when {
                        e.statusCode.is4xxClientError -> FiksClientException(e.rawStatusCode, e.message?.maskerFnr, e)
                        else -> FiksServerException(e.rawStatusCode, e.message?.maskerFnr, e)
                    }
                }
                .block() ?: throw FiksServerException(500, "Fiks - Dokument nedlasting feilet!", null)
        }
        log.info("Hentet dokument (${requestedClass.simpleName}) fra Fiks, dokumentlagerId $dokumentlagerId")
        return dokument
            .also {
                auditService.reportFiks(fnr, "$baseUrl/digisos/api/v1/nav/soknader/$digisosId/dokumenter/$dokumentlagerId", HttpMethod.GET, sporingsId)
                lagreTilCache(dokumentlagerId, it)
            }
    }

    override fun hentAlleDigisosSaker(fnr: String): List<DigisosSak> {
        val sporingsId = genererSporingsId()

        val digisosSaker: List<DigisosSak> = withRetry {
            fiksWebClient.post()
                .uri(PATH_ALLE_DIGISOSSAKER.plus(sporingsIdQuery), sporingsId)
                .headers { it.addAll(fiksHeaders(clientProperties, BEARER + maskinportenClient.getToken())) }
                .body(BodyInserters.fromValue(Fnr(fnr)))
                .retrieve()
                .bodyToMono(typeRef<List<DigisosSak>>())
                .onErrorMap(WebClientResponseException::class.java) { e ->
                    log.warn("Fiks - hentAlleDigisosSaker feilet - ${messageUtenFnr(e)}", e)
                    when {
                        e.statusCode.is4xxClientError -> FiksClientException(e.rawStatusCode, e.message?.maskerFnr, e)
                        else -> FiksServerException(e.rawStatusCode, e.message?.maskerFnr, e)
                    }
                }
                .block() ?: throw FiksServerException(500, "Fiks - AlleDigisosSaker nedlasting feilet!", null)
        }
        log.info("Hentet ${digisosSaker.size} saker fra Fiks (før filter.)")
        return digisosSaker.filter { harKommunenTilgangTilModia(it.kommunenummer) }
            .also { auditService.reportFiks(fnr, baseUrl + PATH_ALLE_DIGISOSSAKER, HttpMethod.POST, sporingsId) }
    }

    private fun harKommunenTilgangTilModia(kommunenummer: String): Boolean {
        log.info(
            "DEBUG: harKommunenTilgangTilModia nr: $kommunenummer unleashB: ${unleash.isEnabled(BERGEN_ENABLED)}" +
                "unleashS: ${unleash.isEnabled(STAVANGER_ENABLED)} nrB: ${clientProperties.bergenKommunenummer}" +
                "nrS: ${clientProperties.stavangerKommunenummer}"
        )
        if (unleash.isEnabled(BERGEN_ENABLED, false) && kommunenummer == clientProperties.bergenKommunenummer) {
            return true
        }
        if (unleash.isEnabled(STAVANGER_ENABLED, false) && kommunenummer == clientProperties.stavangerKommunenummer) {
            return true
        }
        return false
    }

    private fun genererSporingsId(): String = UUID.randomUUID().toString()

    private fun <T> withRetry(block: () -> T): T {
        return runBlocking {
            retry(
                attempts = retryProperties.attempts,
                initialDelay = retryProperties.initialDelay,
                maxDelay = retryProperties.maxDelay,
                retryableExceptions = arrayOf(FiksServerException::class)
            ) {
                block()
            }
        }
    }

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
