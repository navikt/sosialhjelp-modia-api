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
import no.nav.sosialhjelp.modia.client.unleash.FIKS_CACHE_ENABLED
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.feilmeldingUtenFnr
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.logging.AuditService
import no.nav.sosialhjelp.modia.messageUtenFnr
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.service.idporten.IdPortenService
import no.nav.sosialhjelp.modia.typeRef
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.fiksHeaders
import no.nav.sosialhjelp.modia.utils.RequestUtils
import no.nav.sosialhjelp.modia.utils.objectMapper
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.UUID

@Profile("!mock")
@Component
class FiksClientImpl(
    private val fiksWebClient: WebClient,
    private val clientProperties: ClientProperties,
    private val idPortenService: IdPortenService,
    private val auditService: AuditService,
    private val redisService: RedisService,
    private val unleash: Unleash,
    private val retryProperties: FiksRetryProperties,
) : FiksClient {

    private val baseUrl = clientProperties.fiksDigisosEndpointUrl

    override fun hentDigisosSak(digisosId: String): DigisosSak {
        hentDigisosSakFraCache(digisosId)?.let { return it }

        return hentDigisosSakFraFiks(digisosId)
    }

    override fun hentDokument(fnr: String, digisosId: String, dokumentlagerId: String, requestedClass: Class<out Any>): Any {
        hentDokumentFraCache(dokumentlagerId, requestedClass)?.let { return it }

        return hentDokumentFraFiks(fnr, digisosId, dokumentlagerId, requestedClass)
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
        val virksomhetsToken = idPortenService.getToken()
        val sporingsId = genererSporingsId()

        val digisosSak: DigisosSak? = withRetry {
            fiksWebClient.get()
                .uri {
                    it.path(PATH_DIGISOSSAK)
                        .queryParam(SPORINGSID, "{$SPORINGSID}")
                        .build(mapOf(DIGISOSID to digisosId, SPORINGSID to sporingsId))
                }
                .headers { it.addAll(fiksHeaders(clientProperties, BEARER + virksomhetsToken.token)) }
                .retrieve()
                .bodyToMono<DigisosSak>()
                .onErrorMap(WebClientResponseException::class.java) { e ->
                    log.warn("Fiks - hentDigisosSak feilet - ${messageUtenFnr(e)}", e)
                    when {
                        e.statusCode == HttpStatus.NOT_FOUND -> FiksNotFoundException(e.message?.feilmeldingUtenFnr, e)
                        e.statusCode.is4xxClientError -> FiksClientException(e.rawStatusCode, e.message?.feilmeldingUtenFnr, e)
                        else -> FiksServerException(e.rawStatusCode, e.message?.feilmeldingUtenFnr, e)
                    }
                }
                .block()
        }

        log.info("Hentet DigisosSak $digisosId fra Fiks")
        return digisosSak!!
            .also {
                auditService.reportFiks(it.sokerFnr, "$baseUrl/digisos/api/v1/nav/soknader/$digisosId", HttpMethod.GET, sporingsId)
                lagreTilCache(digisosId, it)
            }
    }

    private fun lagreTilCache(id: String, any: Any) {
        if (skalBrukeCache()) {
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
        val virksomhetsToken = idPortenService.getToken()
        val sporingsId = genererSporingsId()

        val dokument: Any? = withRetry {
            fiksWebClient.get()
                .uri {
                    it.path(PATH_DOKUMENT)
                        .queryParam(SPORINGSID, "{$SPORINGSID}")
                        .build(mapOf(DIGISOSID to digisosId, DOKUMENTLAGERID to dokumentlagerId, SPORINGSID to sporingsId))
                }
                .headers { it.addAll(fiksHeaders(clientProperties, BEARER + virksomhetsToken.token)) }
                .retrieve()
                .bodyToMono(requestedClass)
                .onErrorMap(WebClientResponseException::class.java) { e ->
                    log.warn("Fiks - hentDokument feilet - ${messageUtenFnr(e)}", e)
                    when {
                        e.statusCode.is4xxClientError -> FiksClientException(e.rawStatusCode, e.message?.feilmeldingUtenFnr, e)
                        else -> FiksServerException(e.rawStatusCode, e.message?.feilmeldingUtenFnr, e)
                    }
                }
                .block()
        }
        log.info("Hentet dokument (${requestedClass.simpleName}) fra Fiks, dokumentlagerId $dokumentlagerId")
        return dokument!!
            .also {
                auditService.reportFiks(fnr, "$baseUrl/digisos/api/v1/nav/soknader/$digisosId/dokumenter/$dokumentlagerId", HttpMethod.GET, sporingsId)
                lagreTilCache(dokumentlagerId, it)
            }
    }

    override fun hentAlleDigisosSaker(fnr: String): List<DigisosSak> {
        val virksomhetsToken = idPortenService.getToken()
        val sporingsId = genererSporingsId()

        val digisosSaker: List<DigisosSak>? = withRetry {
            fiksWebClient.post()
                .uri {
                    it.path(PATH_ALLE_DIGISOSSAKER)
                        .queryParam(SPORINGSID, "{$SPORINGSID}")
                        .build(mapOf(SPORINGSID to sporingsId))
                }
                .headers { it.addAll(fiksHeaders(clientProperties, BEARER + virksomhetsToken.token)) }
                .body(BodyInserters.fromValue(Fnr(fnr)))
                .retrieve()
                .bodyToMono(typeRef<List<DigisosSak>>())
                .onErrorMap(WebClientResponseException::class.java) { e ->
                    log.warn("Fiks - hentAlleDigisosSaker feilet - ${messageUtenFnr(e)}", e)
                    when {
                        e.statusCode.is4xxClientError -> FiksClientException(e.rawStatusCode, e.message?.feilmeldingUtenFnr, e)
                        else -> FiksServerException(e.rawStatusCode, e.message?.feilmeldingUtenFnr, e)
                    }
                }
                .block()
        }
        return digisosSaker!!
            .also {
                auditService.reportFiks(fnr, baseUrl + PATH_ALLE_DIGISOSSAKER, HttpMethod.POST, sporingsId)
            }
    }

    private fun genererSporingsId(): String {
        return UUID.randomUUID().toString()
    }

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

        //        Query param navn
        private const val SPORINGSID = "sporingsId"
        private const val DIGISOSID = "digisosId"
        private const val DOKUMENTLAGERID = "dokumentlagerId"

        private const val retryAttempts: Int = 5
        private const val initialDelayMillis: Long = 100
        private const val maxDelayMillis: Long = 10000
    }

    private data class Fnr(
        val fnr: String
    )
}
