package no.nav.sbl.sosialhjelpmodiaapi.client.fiks

import kotlinx.coroutines.runBlocking
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksPaths.PATH_ALLE_DIGISOSSAKER
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksPaths.PATH_DIGISOSSAK
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksPaths.PATH_DOKUMENT
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksException
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksNotFoundException
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.feilmeldingUtenFnr
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.logging.AuditService
import no.nav.sbl.sosialhjelpmodiaapi.toFiksErrorMessage
import no.nav.sbl.sosialhjelpmodiaapi.typeRef
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.BEARER
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.fiksHeaders
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.idporten.client.IdPortenClient
import org.joda.time.DateTime
import org.slf4j.MDC
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
        private val idPortenClient: IdPortenClient,
        private val auditService: AuditService
) : FiksClient {

    private val baseUrl = clientProperties.fiksDigisosEndpointUrl

    override fun hentDigisosSak(digisosId: String): DigisosSak {
        log.info("Debug timing: hentDigisosSak inn: ${DateTime.now().millis - (MDC.get("input_timing") ?: "-1").toLong()} | ${MDC.get("RequestId")}")
        val virksomhetsToken = runBlocking { idPortenClient.requestToken() }
        val sporingsId = genererSporingsId()

        log.info("Forsøker å hente digisosSak fra $baseUrl/digisos/api/v1/nav/soknader/$digisosId")
        try {
            val headers = fiksHeaders(clientProperties, BEARER + virksomhetsToken.token)
            val uriComponents = urlWithSporingsId(baseUrl + PATH_DIGISOSSAK)
            val vars = mapOf(DIGISOSID to digisosId, SPORINGSID to sporingsId)

            val start = DateTime.now()
            val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.GET, HttpEntity<Nothing>(headers), DigisosSak::class.java, vars)
            val middle = DateTime.now()

            log.info("Hentet DigisosSak $digisosId fra Fiks")
            val digisosSak = response.body!!

            auditService.reportFiks(digisosSak.sokerFnr, "$baseUrl/digisos/api/v1/nav/soknader/$digisosId", HttpMethod.GET, sporingsId)
            log.info("Debug timing: hentDigisosSak Pre: ${start.millis - (MDC.get("input_timing") ?: "-1").toLong()} Fiks: ${middle.millis-start.millis} Audit: ${DateTime.now().millis-middle.millis} | ${start.millis} | ${MDC.get("RequestId")}")

            return digisosSak

        } catch (e: HttpStatusCodeException) {
            val fiksErrorMessage = e.toFiksErrorMessage()?.feilmeldingUtenFnr
            val message = e.message?.feilmeldingUtenFnr
            if (e.statusCode == HttpStatus.NOT_FOUND) {
                throw FiksNotFoundException(e.message, e, digisosId)
            }
            log.warn("Fiks - hentDigisosSak feilet for id $digisosId - ${e.statusCode} $message - $fiksErrorMessage", e)
            throw FiksException(e.message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentDigisosSak feilet", e)
            throw FiksException(e.message, e)
        }
    }

    override fun hentDokument(fnr: String, digisosId: String, dokumentlagerId: String, requestedClass: Class<out Any>): Any {
        val virksomhetsToken = runBlocking { idPortenClient.requestToken() }
        val sporingsId = genererSporingsId()

        log.info("Forsøker å hente dokument fra $baseUrl/digisos/api/v1/nav/soknader/$digisosId/dokumenter/$dokumentlagerId")
        try {
            val headers = fiksHeaders(clientProperties, BEARER + virksomhetsToken.token)
            val uriComponents = urlWithSporingsId(baseUrl + PATH_DOKUMENT)
            val vars = mapOf(
                    DIGISOSID to digisosId,
                    DOKUMENTLAGERID to dokumentlagerId,
                    SPORINGSID to sporingsId)

            val start = DateTime.now()
            val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.GET, HttpEntity<Nothing>(headers), requestedClass, vars)
            val middle = DateTime.now()

            auditService.reportFiks(fnr, "$baseUrl/digisos/api/v1/nav/soknader/$digisosId/dokumenter/$dokumentlagerId", HttpMethod.GET, sporingsId)
            log.info("Debug timing: hentDokument Pre: ${start.millis - (MDC.get("input_timing") ?: "-1").toLong()} Fiks: ${middle.millis-start.millis} Audit: ${DateTime.now().millis-middle.millis} | ${start.millis} | ${MDC.get("RequestId") ?: ""}")

            log.info("Hentet dokument (${requestedClass.simpleName}) fra Fiks, dokumentlagerId $dokumentlagerId")
            return response.body!!

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
        val virksomhetsToken = runBlocking { idPortenClient.requestToken() }
        val sporingsId = genererSporingsId()
        try {
            val headers = fiksHeaders(clientProperties, BEARER + virksomhetsToken.token)
            val urlTemplate = baseUrl + PATH_ALLE_DIGISOSSAKER
            val uriComponents = urlWithSporingsId(urlTemplate)
            val vars = mapOf(SPORINGSID to sporingsId)
            val body = Fnr(fnr)

            val start = DateTime.now()
            val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.POST, HttpEntity(body, headers), typeRef<List<DigisosSak>>(), vars)
            val middle = DateTime.now()

            auditService.reportFiks(fnr, urlTemplate, HttpMethod.POST, sporingsId)
            log.info("Debug timing: hentAlleDigisosSaker Pre: ${start.millis - (MDC.get("input_timing") ?: "-1").toLong()} Fiks: ${middle.millis-start.millis} Audit: ${DateTime.now().millis-middle.millis} | ${start.millis} | ${MDC.get("RequestId") ?: ""}")

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
