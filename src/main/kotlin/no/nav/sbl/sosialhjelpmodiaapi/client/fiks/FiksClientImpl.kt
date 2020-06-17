package no.nav.sbl.sosialhjelpmodiaapi.client.fiks

import kotlinx.coroutines.runBlocking
import no.nav.sbl.sosialhjelpmodiaapi.client.idporten.IdPortenService
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksException
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksNotFoundException
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.feilmeldingUtenFnr
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.toFiksErrorMessage
import no.nav.sbl.sosialhjelpmodiaapi.typeRef
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.BEARER
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.HEADER_INTEGRASJON_ID
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.HEADER_INTEGRASJON_PASSORD
import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import java.util.Collections.singletonList


@Profile("!mock")
@Component
class FiksClientImpl(
        clientProperties: ClientProperties,
        private val restTemplate: RestTemplate,
        private val idPortenService: IdPortenService
) : FiksClient {

    private val baseUrl = clientProperties.fiksDigisosEndpointUrl
    private val fiksIntegrasjonid = clientProperties.fiksIntegrasjonId
    private val fiksIntegrasjonpassord = clientProperties.fiksIntegrasjonpassord

    override fun hentDigisosSak(digisosId: String): DigisosSak {
        val virksomhetsToken = runBlocking { idPortenService.requestToken() }
        val sporingsId = genererSporingsId()

        log.info("Forsøker å hente digisosSak fra $baseUrl/digisos/api/v1/nav/soknader/$digisosId")
        try {
            val headers = setIntegrasjonHeaders(BEARER + virksomhetsToken.token)
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
            val headers = setIntegrasjonHeaders(BEARER + virksomhetsToken.token)
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
            val headers = setIntegrasjonHeaders(BEARER + virksomhetsToken.token)
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

    override fun hentKommuneInfo(kommunenummer: String): KommuneInfo {
        val virksomhetsToken = runBlocking { idPortenService.requestToken() }

        try {
            val headers = setIntegrasjonHeaders(BEARER + virksomhetsToken.token)
            val urlTemplate = baseUrl + PATH_KOMMUNEINFO
            val vars = mapOf(KOMMUNENUMMER to kommunenummer)

            val response = restTemplate.exchange(urlTemplate, HttpMethod.GET, HttpEntity<Nothing>(headers), KommuneInfo::class.java, vars)

            return response.body!!

        } catch (e: HttpStatusCodeException) {
            val fiksErrorMessage = e.toFiksErrorMessage()?.feilmeldingUtenFnr
            val message = e.message?.feilmeldingUtenFnr
            log.warn("Fiks - hentKommuneInfo feilet - $message - $fiksErrorMessage", e)
            throw FiksException(e.statusCode, e.message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentKommuneInfo feilet", e)
            throw FiksException(null, e.message, e)
        }
    }

    override fun hentKommuneInfoForAlle(): List<KommuneInfo> {
        val virksomhetsToken = runBlocking { idPortenService.requestToken() }

        try {
            val headers = setIntegrasjonHeaders(BEARER + virksomhetsToken.token)
            val urlTemplate = baseUrl + PATH_ALLE_KOMMUNEINFO

            val response = restTemplate.exchange(urlTemplate, HttpMethod.GET, HttpEntity<Nothing>(headers), typeRef<List<KommuneInfo>>())

            return response.body!!

        } catch (e: HttpStatusCodeException) {
            val fiksErrorMessage = e.toFiksErrorMessage()?.feilmeldingUtenFnr
            val message = e.message?.feilmeldingUtenFnr
            log.warn("Fiks - hentKommuneInfoForAlle feilet - $message - $fiksErrorMessage", e)
            throw FiksException(e.statusCode, message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentKommuneInfoForAlle feilet", e)
            throw FiksException(null, e.message?.feilmeldingUtenFnr, e)
        }
    }

    private fun setIntegrasjonHeaders(token: String): HttpHeaders {
        val headers = HttpHeaders()
        headers.accept = singletonList(MediaType.APPLICATION_JSON)
        headers.set(AUTHORIZATION, token)
        headers.set(HEADER_INTEGRASJON_ID, fiksIntegrasjonid)
        headers.set(HEADER_INTEGRASJON_PASSORD, fiksIntegrasjonpassord)
        return headers
    }

    private fun genererSporingsId(): String {
        return UUID.randomUUID().toString()
    }

    private fun urlWithSporingsId(urlTemplate: String) =
            UriComponentsBuilder.fromHttpUrl(urlTemplate).queryParam(SPORINGSID, "{$SPORINGSID}").build()

    companion object {
        private val log by logger()

//        Paths til fiks-api
        private const val PATH_DIGISOSSAK = "/digisos/api/v1/nav/soknader/{digisosId}"
        private const val PATH_ALLE_DIGISOSSAKER = "/digisos/api/v1/nav/soknader/soknader"
        private const val PATH_DOKUMENT = "/digisos/api/v1/nav/soknader/{digisosId}/dokumenter/{dokumentlagerId}"
        private const val PATH_KOMMUNEINFO = "/digisos/api/v1/nav/kommuner/{kommunenummer}"
        private const val PATH_ALLE_KOMMUNEINFO = "/digisos/api/v1/nav/kommuner"

//        Query param navn
        private const val SPORINGSID = "sporingsId"
        private const val DIGISOSID = "digisosId"
        private const val DOKUMENTLAGERID = "dokumentlagerId"
        private const val KOMMUNENUMMER = "kommunenummer"
    }

    private data class Fnr(
            val fnr: String
    )
}
