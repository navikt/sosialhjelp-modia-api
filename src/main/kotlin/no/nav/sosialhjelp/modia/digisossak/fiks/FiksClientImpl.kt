package no.nav.sosialhjelp.modia.digisossak.fiks

import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.exceptions.FiksClientException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksNotFoundException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksServerException
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.auth.texas.TexasClient
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksPaths.PATH_ALLE_DIGISOSSAKER
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksPaths.PATH_DIGISOSSAK
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksPaths.PATH_DOKUMENT
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.logging.AuditService
import no.nav.sosialhjelp.modia.maskerFnr
import no.nav.sosialhjelp.modia.redis.RedisKeyType
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.utils.IntegrationUtils
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import no.nav.sosialhjelp.modia.utils.RequestUtils
import no.nav.sosialhjelp.modia.utils.sosialhjelpJsonMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@Component
class FiksClientImpl(
    private val fiksRestClient: RestClient,
    private val clientProperties: ClientProperties,
    private val auditService: AuditService,
    private val redisService: RedisService,
    @Value("\${dokument_cache_time_to_live_seconds}") private val documentTTL: Long,
    private val texasClient: TexasClient,
    private val fiksRetryTemplate: RetryTemplate,
) : FiksClient {
    private val baseUrl = clientProperties.fiksDigisosEndpointUrl

    override fun hentDigisosSak(digisosId: String): DigisosSak =
        hentDigisosSakFraCache(digisosId)?.also { log.info("Hentet digisosSak=$digisosId fra cache") }
            ?: hentDigisosSakFraFiks(digisosId)

    override fun <T : Any> hentDokument(
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

    private fun hentDigisosSakFraFiks(digisosId: String): DigisosSak {
        val sporingsId = genererSporingsId()

        val digisosSak: DigisosSak =
            try {
                fiksRetryTemplate.execute<DigisosSak, Exception> {
                    fiksRestClient
                        .get()
                        .uri(PATH_DIGISOSSAK.plus(sporingsIdQuery), digisosId, sporingsId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(IntegrationUtils.HEADER_INTEGRASJON_ID, clientProperties.fiksIntegrasjonId)
                        .header(IntegrationUtils.HEADER_INTEGRASJON_PASSORD, clientProperties.fiksIntegrasjonpassord)
                        .header("Authorization", BEARER + texasClient.getMaskinportenToken())
                        .retrieve()
                        .body<DigisosSak>()
                        ?: throw FiksServerException(500, "Fiks - DigisosSak nedlasting feilet!", null)
                }
            } catch (e: HttpClientErrorException) {
                log.warn("Fiks - hentDigisosSak feilet - ${e.message?.maskerFnr}", e)
                if (e.statusCode == HttpStatus.NOT_FOUND) {
                    throw FiksNotFoundException(e.message?.maskerFnr, e)
                }
                throw FiksClientException(e.statusCode.value(), e.message?.maskerFnr, e)
            } catch (e: HttpServerErrorException) {
                log.warn("Fiks - hentDigisosSak feilet - ${e.message?.maskerFnr}", e)
                throw FiksServerException(e.statusCode.value(), e.message?.maskerFnr, e)
            }

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

    private fun <T : Any> hentDokumentFraFiks(
        fnr: String,
        digisosId: String,
        dokumentlagerId: String,
        requestedClass: Class<out T>,
        cacheKey: String,
    ): T {
        val sporingsId = genererSporingsId()

        val dokument =
            try {
                fiksRetryTemplate.execute<T, Exception> {
                    fiksRestClient
                        .get()
                        .uri(PATH_DOKUMENT.plus(sporingsIdQuery), digisosId, dokumentlagerId, sporingsId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(IntegrationUtils.HEADER_INTEGRASJON_ID, clientProperties.fiksIntegrasjonId)
                        .header(IntegrationUtils.HEADER_INTEGRASJON_PASSORD, clientProperties.fiksIntegrasjonpassord)
                        .header("Authorization", BEARER + texasClient.getMaskinportenToken())
                        .retrieve()
                        .body(requestedClass)
                        ?: throw FiksServerException(500, "Fiks - Dokument nedlasting feilet!", null)
                }
            } catch (e: HttpClientErrorException) {
                log.warn("Fiks - hentDokument feilet - ${e.message?.maskerFnr}", e)
                throw FiksClientException(e.statusCode.value(), e.message?.maskerFnr, e)
            } catch (e: HttpServerErrorException) {
                log.warn("Fiks - hentDokument feilet - ${e.message?.maskerFnr}", e)
                throw FiksServerException(e.statusCode.value(), e.message?.maskerFnr, e)
            }

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

    override fun hentAlleDigisosSaker(fnr: String): List<DigisosSak> {
        val sporingsId = genererSporingsId()

        val digisosSaker: List<DigisosSak> =
            try {
                fiksRetryTemplate.execute<List<DigisosSak>, Exception> {
                    fiksRestClient
                        .post()
                        .uri(PATH_ALLE_DIGISOSSAKER.plus(sporingsIdQuery), sporingsId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(IntegrationUtils.HEADER_INTEGRASJON_ID, clientProperties.fiksIntegrasjonId)
                        .header(IntegrationUtils.HEADER_INTEGRASJON_PASSORD, clientProperties.fiksIntegrasjonpassord)
                        .header("Authorization", BEARER + texasClient.getMaskinportenToken())
                        .body(Fnr(fnr))
                        .retrieve()
                        .body(object : ParameterizedTypeReference<List<DigisosSak>>() {})
                        ?: throw FiksServerException(500, "Fiks - AlleDigisosSaker nedlasting feilet!", null)
                }
            } catch (e: HttpClientErrorException) {
                log.warn("Fiks - hentAlleDigisosSaker feilet - ${e.message?.maskerFnr}", e)
                throw FiksClientException(e.statusCode.value(), e.message?.maskerFnr, e)
            } catch (e: HttpServerErrorException) {
                log.warn("Fiks - hentAlleDigisosSaker feilet - ${e.message?.maskerFnr}", e)
                throw FiksServerException(e.statusCode.value(), e.message?.maskerFnr, e)
            }

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
