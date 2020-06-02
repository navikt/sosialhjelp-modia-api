package no.nav.sbl.sosialhjelpmodiaapi.client.fiks

import kotlinx.coroutines.runBlocking
import no.nav.sbl.sosialhjelpmodiaapi.client.idporten.IdPortenService
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksException
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksNotFoundException
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.domain.DigisosSak
import no.nav.sbl.sosialhjelpmodiaapi.domain.KommuneInfo
import no.nav.sbl.sosialhjelpmodiaapi.feilmeldingUtenFnr
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.toFiksErrorResponse
import no.nav.sbl.sosialhjelpmodiaapi.typeRef
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.BEARER
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.HEADER_INTEGRASJON_ID
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.HEADER_INTEGRASJON_PASSORD
import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
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

    override fun hentDigisosSak(digisosId: String, sporingsId: String): DigisosSak {
        val virksomhetsToken = runBlocking { idPortenService.requestToken() }

        val headers = setIntegrasjonHeaders(BEARER + virksomhetsToken.token)

        log.info("Forsøker å hente digisosSak fra $baseUrl/digisos/api/v1/nav/soknader/$digisosId")

        val urlTemplate = "$baseUrl/digisos/api/v1/nav/soknader/{digisosId}/{sporingsId}"
        try {
            val vars = mapOf("digisosId" to digisosId, "sporingsId" to sporingsId)
            val response = restTemplate.exchange(urlTemplate, HttpMethod.GET, HttpEntity<Nothing>(headers), String::class.java, vars)

            log.info("Hentet DigisosSak $digisosId fra Fiks")
            return objectMapper.readValue(response.body!!, DigisosSak::class.java)

        } catch (e: HttpStatusCodeException) {
            val fiksErrorResponse = e.toFiksErrorResponse()?.feilmeldingUtenFnr
            val errorMessage = e.message?.feilmeldingUtenFnr
            log.warn("Fiks - hentDigisosSak feilet for id $digisosId - $errorMessage - $fiksErrorResponse", e)
            if (e.statusCode == HttpStatus.NOT_FOUND) {
                throw FiksNotFoundException(e.statusCode, e.message, e)
            }
            throw FiksException(e.statusCode, e.message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentDigisosSak feilet", e)
            throw FiksException(null, e.message, e)
        }
    }

    override fun hentDokument(digisosId: String, dokumentlagerId: String, requestedClass: Class<out Any>, sporingsId: String): Any {
        val virksomhetsToken = runBlocking { idPortenService.requestToken() }

        val headers = setIntegrasjonHeaders(BEARER + virksomhetsToken.token)

        val urlTemplate = "$baseUrl/digisos/api/v1/nav/soknader/{digisosId}/dokumenter/{dokumentlagerId}"
        val uriComponents = UriComponentsBuilder.fromHttpUrl(urlTemplate).queryParam("sporingsId", "%7BsporingsId%7D").build()
        log.info("Forsøker å hente dokument fra $baseUrl/digisos/api/v1/nav/soknader/$digisosId/dokumenter/$dokumentlagerId")
        try {
            val vars = mapOf(
                    "digisosId" to digisosId,
                    "dokumentlagerId" to dokumentlagerId,
                    "sporingsId" to sporingsId)
            val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.GET, HttpEntity<Nothing>(headers), String::class.java, vars)

            log.info("Hentet dokument (${requestedClass.simpleName}) fra Fiks, dokumentlagerId $dokumentlagerId")
            return objectMapper.readValue(response.body!!, requestedClass)

        } catch (e: HttpStatusCodeException) {
            val fiksErrorResponse = e.toFiksErrorResponse()?.feilmeldingUtenFnr
            val errorMessage = e.message?.feilmeldingUtenFnr
            log.warn("Fiks - hentDokument feilet - $errorMessage - $fiksErrorResponse", e)
            throw FiksException(e.statusCode, e.message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentDokument feilet", e)
            throw FiksException(null, e.message, e)
        }
    }

    override fun hentAlleDigisosSaker(sporingsId: String, fnr: String): List<DigisosSak> {
        val virksomhetsToken = runBlocking { idPortenService.requestToken() }

        val headers = setIntegrasjonHeaders(BEARER + virksomhetsToken.token)

        val urlTemplate = "$baseUrl/digisos/api/nav/v1/soknader/soknader/{sporingsId}"
        try {
            val vars = mapOf("sporingsId" to sporingsId)
            val body = Fnr(fnr)
            val response = restTemplate.exchange(urlTemplate, HttpMethod.POST, HttpEntity(body, headers), typeRef<List<DigisosSak>>(), vars)

            return response.body!!

        } catch (e: HttpStatusCodeException) {
            val fiksErrorResponse = e.toFiksErrorResponse()?.feilmeldingUtenFnr
            val errorMessage = e.message?.feilmeldingUtenFnr
            log.warn("Fiks - hentAlleDigisosSaker feilet - $errorMessage - $fiksErrorResponse", e)
            throw FiksException(e.statusCode, e.message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentAlleDigisosSaker feilet", e)
            throw FiksException(null, e.message, e)
        }
    }

    override fun hentKommuneInfo(kommunenummer: String): KommuneInfo {
        val virksomhetsToken = runBlocking { idPortenService.requestToken() }

        val headers = setIntegrasjonHeaders(BEARER + virksomhetsToken.token)

        val urlTemplate = "$baseUrl/digisos/api/v1/nav/kommuner/{kommunenummer}"
        try {
            val vars = mapOf("kommunenummer" to kommunenummer)
            val response = restTemplate.exchange(urlTemplate, HttpMethod.GET, HttpEntity<Nothing>(headers), KommuneInfo::class.java, vars)

            return response.body!!

        } catch (e: HttpStatusCodeException) {
            val fiksErrorResponse = e.toFiksErrorResponse()?.feilmeldingUtenFnr
            val errorMessage = e.message?.feilmeldingUtenFnr
            log.warn("Fiks - hentKommuneInfo feilet - $errorMessage - $fiksErrorResponse", e)
            throw FiksException(e.statusCode, e.message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentKommuneInfo feilet", e)
            throw FiksException(null, e.message, e)
        }
    }

    override fun hentKommuneInfoForAlle(): List<KommuneInfo> {
        val virksomhetsToken = runBlocking { idPortenService.requestToken() }

        val headers = setIntegrasjonHeaders(BEARER + virksomhetsToken.token)

        try {
            val response = restTemplate.exchange("$baseUrl/digisos/api/v1/nav/kommuner", HttpMethod.GET, HttpEntity<Nothing>(headers), typeRef<List<KommuneInfo>>())

            return response.body!!

        } catch (e: HttpStatusCodeException) {
            val fiksErrorResponse = e.toFiksErrorResponse()?.feilmeldingUtenFnr
            val errorMessage = e.message?.feilmeldingUtenFnr
            log.warn("Fiks - hentKommuneInfoForAlle feilet - $errorMessage - $fiksErrorResponse", e)
            throw FiksException(e.statusCode, errorMessage, e)
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

    companion object {
        private val log by logger()
    }

    private data class Fnr(
            val fnr: String
    )
}
