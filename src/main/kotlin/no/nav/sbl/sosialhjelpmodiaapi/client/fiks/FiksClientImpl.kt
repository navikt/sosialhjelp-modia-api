package no.nav.sbl.sosialhjelpmodiaapi.client.fiks

import kotlinx.coroutines.runBlocking
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksPaths.PATH_ALLE_DIGISOSSAKER
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksPaths.PATH_DIGISOSSAK
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksPaths.PATH_DOKUMENT
import no.nav.sbl.sosialhjelpmodiaapi.client.idporten.IdPortenService
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksException
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksNotFoundException
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.feilmeldingUtenFnr
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.toFiksErrorMessage
import no.nav.sbl.sosialhjelpmodiaapi.typeRef
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.BEARER
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.fiksHeaders
import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
import no.nav.sosialhjelp.api.fiks.DigisosSak
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
        private val idPortenService: IdPortenService
) : FiksClient {

    private val baseUrl = clientProperties.fiksDigisosEndpointUrl

    override fun hentDigisosSak(digisosId: String): DigisosSak {
        val virksomhetsToken = runBlocking { idPortenService.requestToken() }
        val sporingsId = genererSporingsId()

        log.info("Forsøker å hente digisosSak fra $baseUrl/digisos/api/v1/nav/soknader/$digisosId")
        try {
            val headers = fiksHeaders(clientProperties, BEARER + virksomhetsToken.token)
            val uriComponents = urlWithSporingsId(baseUrl + PATH_DIGISOSSAK)
            val vars = mapOf(DIGISOSID to digisosId, SPORINGSID to sporingsId)

            val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.GET, HttpEntity<Nothing>(headers), String::class.java, vars)

            log.info("Hentet DigisosSak $digisosId fra Fiks")
            return objectMapper.readValue(response.body!!, DigisosSak::class.java)

        } catch (e: HttpStatusCodeException) {
            val fiksErrorMessage = e.toFiksErrorMessage()?.feilmeldingUtenFnr
            val message = e.message?.feilmeldingUtenFnr
            if (e.statusCode == HttpStatus.NOT_FOUND) {
                throw FiksNotFoundException(e.statusCode, e.message, e, digisosId)
            }
            log.warn("Fiks - hentDigisosSak feilet for id $digisosId - $message - $fiksErrorMessage", e)
            throw FiksException(e.statusCode, e.message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentDigisosSak feilet", e)
            throw FiksException(null, e.message, e)
        }
    }

    override fun hentDokument(digisosId: String, dokumentlagerId: String, requestedClass: Class<out Any>): Any {
        val virksomhetsToken = runBlocking { idPortenService.requestToken() }
        val sporingsId = genererSporingsId()

        log.info("Forsøker å hente dokument fra $baseUrl/digisos/api/v1/nav/soknader/$digisosId/dokumenter/$dokumentlagerId")
        try {
            val headers = fiksHeaders(clientProperties, BEARER + virksomhetsToken.token)
            val uriComponents = urlWithSporingsId(baseUrl + PATH_DOKUMENT)
            val vars = mapOf(
                    DIGISOSID to digisosId,
                    DOKUMENTLAGERID to dokumentlagerId,
                    SPORINGSID to sporingsId)

            val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.GET, HttpEntity<Nothing>(headers), String::class.java, vars)

            log.info("Hentet dokument (${requestedClass.simpleName}) fra Fiks, dokumentlagerId $dokumentlagerId")
            return objectMapper.readValue(response.body!!, requestedClass)

        } catch (e: HttpStatusCodeException) {
            val fiksErrorMessage = e.toFiksErrorMessage()?.feilmeldingUtenFnr
            val message = e.message?.feilmeldingUtenFnr
            log.warn("Fiks - hentDokument feilet - $message - $fiksErrorMessage", e)
            throw FiksException(e.statusCode, e.message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentDokument feilet", e)
            throw FiksException(null, e.message, e)
        }
    }

    override fun hentAlleDigisosSaker(fnr: String): List<DigisosSak> {
        val virksomhetsToken = runBlocking { idPortenService.requestToken() }
        val sporingsId = genererSporingsId()
        try {
            val headers = fiksHeaders(clientProperties, BEARER + virksomhetsToken.token)
            val uriComponents = urlWithSporingsId(baseUrl + PATH_ALLE_DIGISOSSAKER)
            val vars = mapOf(SPORINGSID to sporingsId)
            val body = Fnr(fnr)

            val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.POST, HttpEntity(body, headers), typeRef<List<DigisosSak>>(), vars)

            return response.body!!

        } catch (e: HttpStatusCodeException) {
            val fiksErrorMessage = e.toFiksErrorMessage()?.feilmeldingUtenFnr
            val message = e.message?.feilmeldingUtenFnr
            log.warn("Fiks - hentAlleDigisosSaker feilet - $message - $fiksErrorMessage", e)
            throw FiksException(e.statusCode, e.message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentAlleDigisosSaker feilet", e)
            throw FiksException(null, e.message, e)
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
