package no.nav.sbl.sosialhjelpmodiaapi.client.fiks

import no.finn.unleash.Unleash
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksPaths.PATH_ALLE_DIGISOSSAKER
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksPaths.PATH_DIGISOSSAK
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksPaths.PATH_DOKUMENT
import no.nav.sbl.sosialhjelpmodiaapi.client.unleash.FIKS_CACHE_ENABLED
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.feilmeldingUtenFnr
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.logging.AuditService
import no.nav.sbl.sosialhjelpmodiaapi.redis.RedisService
import no.nav.sbl.sosialhjelpmodiaapi.service.idporten.IdPortenService
import no.nav.sbl.sosialhjelpmodiaapi.toFiksErrorMessage
import no.nav.sbl.sosialhjelpmodiaapi.typeRef
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.BEARER
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.fiksHeaders
import no.nav.sbl.sosialhjelpmodiaapi.utils.TokenUtils
import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksNotFoundException
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*


@Profile("!mock")
@Component
class FiksClientImpl(
        private val clientProperties: ClientProperties,
        private val restTemplate: RestTemplate,
        private val idPortenService: IdPortenService,
        private val auditService: AuditService,
        private val redisService: RedisService,
        private val tokenUtils: TokenUtils,
        private val unleash: Unleash,
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
        return unleash.isEnabled(FIKS_CACHE_ENABLED, false)
    }

    private fun hentDigisosSakFraCache(digisosId: String): DigisosSak? {
        if (skalBrukeCache()) {
            log.debug("Forsøker å hente digisosSak fra cache")
            return redisService.get(cacheKeyFor(digisosId), DigisosSak::class.java) as DigisosSak?
        }
        return null
    }

    // todo: endre fra navIdent til sessionId
    // cache key = "<NavIdent>_<digisosId>" eller "<NavIdent>_<dokumentlagerId>"
    private fun cacheKeyFor(id: String) = "${tokenUtils.hentNavIdentForInnloggetBruker()}_$id"

    private fun hentDigisosSakFraFiks(digisosId: String): DigisosSak {
        val virksomhetsToken = idPortenService.getToken()
        val sporingsId = genererSporingsId()

        try {
            val headers = fiksHeaders(clientProperties, BEARER + virksomhetsToken.token)
            val uriComponents = urlWithSporingsId(baseUrl + PATH_DIGISOSSAK)
            val vars = mapOf(DIGISOSID to digisosId, SPORINGSID to sporingsId)

            val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.GET, HttpEntity<Nothing>(headers), DigisosSak::class.java, vars)

            log.info("Hentet DigisosSak $digisosId fra Fiks")
            val digisosSak = response.body!!

            auditService.reportFiks(digisosSak.sokerFnr, "$baseUrl/digisos/api/v1/nav/soknader/$digisosId", HttpMethod.GET, sporingsId)

            return digisosSak
                    .also { lagreTilCache(digisosId, it) }

        } catch (e: HttpStatusCodeException) {
            val fiksErrorMessage = e.toFiksErrorMessage()?.feilmeldingUtenFnr
            val message = e.message?.feilmeldingUtenFnr
            if (e.statusCode == HttpStatus.NOT_FOUND) {
                throw FiksNotFoundException(e.message, e)
            }
            log.warn("Fiks - hentDigisosSak feilet for id $digisosId - ${e.statusCode} $message - $fiksErrorMessage", e)
            throw FiksException(e.message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentDigisosSak feilet for id $digisosId", e)
            throw FiksException(e.message, e)
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

        try {
            val headers = fiksHeaders(clientProperties, BEARER + virksomhetsToken.token)
            val uriComponents = urlWithSporingsId(baseUrl + PATH_DOKUMENT)
            val vars = mapOf(
                    DIGISOSID to digisosId,
                    DOKUMENTLAGERID to dokumentlagerId,
                    SPORINGSID to sporingsId)

            val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.GET, HttpEntity<Nothing>(headers), requestedClass, vars)

            auditService.reportFiks(fnr, "$baseUrl/digisos/api/v1/nav/soknader/$digisosId/dokumenter/$dokumentlagerId", HttpMethod.GET, sporingsId)

            log.info("Hentet dokument (${requestedClass.simpleName}) fra Fiks, dokumentlagerId $dokumentlagerId")
            return response.body!!
                    .also { lagreTilCache(dokumentlagerId, it) }

        } catch (e: HttpStatusCodeException) {
            val fiksErrorMessage = e.toFiksErrorMessage()?.feilmeldingUtenFnr
            val message = e.message?.feilmeldingUtenFnr
            log.warn("Fiks - hentDokument feilet - ${e.statusCode} $message - $fiksErrorMessage", e)
            throw FiksException(e.message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentDokument feilet", e)
            throw FiksException(e.message, e)
        }
    }

    override fun hentAlleDigisosSaker(fnr: String): List<DigisosSak> {
        val virksomhetsToken = idPortenService.getToken()
        val sporingsId = genererSporingsId()
        try {
            val headers = fiksHeaders(clientProperties, BEARER + virksomhetsToken.token)
            val urlTemplate = baseUrl + PATH_ALLE_DIGISOSSAKER
            val uriComponents = urlWithSporingsId(urlTemplate)
            val vars = mapOf(SPORINGSID to sporingsId)
            val body = Fnr(fnr)

            val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.POST, HttpEntity(body, headers), typeRef<List<DigisosSak>>(), vars)

            auditService.reportFiks(fnr, urlTemplate, HttpMethod.POST, sporingsId)

            return response.body!!

        } catch (e: HttpStatusCodeException) {
            val fiksErrorMessage = e.toFiksErrorMessage()?.feilmeldingUtenFnr
            val message = e.message?.feilmeldingUtenFnr
            log.warn("Fiks - hentAlleDigisosSaker feilet - ${e.statusCode} $message - $fiksErrorMessage", e)
            throw FiksException(e.message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentAlleDigisosSaker feilet", e)
            throw FiksException(e.message, e)
        }
    }

    private fun genererSporingsId(): String {
        return UUID.randomUUID().toString()
    }

    private fun urlWithSporingsId(urlTemplate: String) =
            UriComponentsBuilder.fromUriString(urlTemplate).queryParam(SPORINGSID, "{$SPORINGSID}").build()

    companion object {
        private val log by logger()

        //        Query param navn
        private const val SPORINGSID = "sporingsId"
        private const val DIGISOSID = "digisosId"
        private const val DOKUMENTLAGERID = "dokumentlagerId"
    }

    private data class Fnr(
            val fnr: String
    )
}
