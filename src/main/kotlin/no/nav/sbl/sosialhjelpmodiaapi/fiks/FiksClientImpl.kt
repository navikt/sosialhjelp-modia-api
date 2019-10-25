package no.nav.sbl.sosialhjelpmodiaapi.fiks

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.runBlocking
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksException
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.domain.DigisosSak
import no.nav.sbl.sosialhjelpmodiaapi.domain.KommuneInfo
import no.nav.sbl.sosialhjelpmodiaapi.idporten.IdPortenService
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.typeRef
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.HEADER_INTEGRASJON_ID
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.HEADER_INTEGRASJON_PASSORD
import no.nav.sbl.sosialhjelpmodiaapi.utils.filformatObjectMapper
import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
import org.springframework.context.annotation.Profile
import org.springframework.http.*
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.lang.NonNull
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.Collections.singletonList


@Profile("!mock")
@Component
class FiksClientImpl(clientProperties: ClientProperties,
                     private val restTemplate: RestTemplate,
                     private val idPortenService: IdPortenService) : FiksClient {

    companion object {
        val log by logger()
    }

    private val baseUrl = clientProperties.fiksDigisosEndpointUrl
    private val fiksIntegrasjonid = clientProperties.fiksIntegrasjonId
    private val fiksIntegrasjonpassord = clientProperties.fiksIntegrasjonpassord

    override fun hentDigisosSak(digisosId: String, sporingsId: String): DigisosSak {
        val virksomhetsToken = runBlocking { idPortenService.requestToken() }

        val headers = setIntegrasjonHeaders("Bearer ${virksomhetsToken.token}")

        log.info("Forsøker å hente digisosSak fra $baseUrl/digisos/api/v1/nav/soknader/$digisosId")

        val url = "$baseUrl/digisos/api/v1/nav/soknader/$digisosId"
        val uriComponents = UriComponentsBuilder.fromHttpUrl(url).queryParam("sporingsId", sporingsId).build()
        try {
            val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.GET, HttpEntity<Nothing>(headers), String::class.java)

            log.info("Hentet DigisosSak $digisosId fra Fiks")
            return objectMapper.readValue(response.body!!, DigisosSak::class.java)

        } catch (e: HttpStatusCodeException) {
            log.warn("Fiks - hentDigisosSak feilet - ${e.statusCode} ${e.statusText}", e)
            throw FiksException(e.statusCode, e.message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentDigisosSak feilet", e)
            throw FiksException(null, e.message, e)
        }
    }

    /**
     * Brukes for å hente json-filer som er definert i filformat. Dermed brukes filformatObjectMapper
     */
    override fun hentDokument(digisosId: String, dokumentlagerId: String, requestedClass: Class<out Any>, sporingsId: String): Any {
        val virksomhetsToken = runBlocking { idPortenService.requestToken() }

        val headers = setIntegrasjonHeaders("Bearer ${virksomhetsToken.token}")

        val url = "$baseUrl/digisos/api/v1/nav/soknader/$digisosId/dokumenter/$dokumentlagerId"
        val uriComponents = UriComponentsBuilder.fromHttpUrl(url).queryParam("sporingsId", sporingsId).build()
        log.info("Forsøker å hente dokument fra $url")
        try {
            val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.GET, HttpEntity<Nothing>(headers), String::class.java)

            log.info("Hentet dokument (${requestedClass.simpleName}) fra Fiks, dokumentlagerId $dokumentlagerId")
            return filformatObjectMapper.readValue(response.body!!, requestedClass)

        } catch (e: HttpStatusCodeException) {
            log.warn("Fiks - hentDokument feilet - ${e.statusCode} ${e.statusText}", e)
            throw FiksException(e.statusCode, e.message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentDokument feilet", e)
            throw FiksException(null, e.message, e)
        }
    }

    override fun hentAlleDigisosSaker(sporingsId: String): List<DigisosSak> {
        val virksomhetsToken = runBlocking { idPortenService.requestToken() }

        val headers = setIntegrasjonHeaders("Bearer ${virksomhetsToken.token}")

        val url = "$baseUrl/digisos/api/nav/v1/soknader"
        val uriComponents = UriComponentsBuilder.fromHttpUrl(url).queryParam("sporingsId", sporingsId).build()
        try {
            val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.GET, HttpEntity<Nothing>(headers), typeRef<List<String>>())

            return response.body!!.map { s: String -> objectMapper.readValue(s, DigisosSak::class.java) }

        } catch (e: HttpStatusCodeException) {
            log.warn("Fiks - hentAlleDigisosSaker feilet - ${e.statusCode} ${e.statusText}", e)
            throw FiksException(e.statusCode, e.message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentAlleDigisosSaker feilet", e)
            throw FiksException(null, e.message, e)
        }
    }

    override fun hentKommuneInfo(kommunenummer: String): KommuneInfo {
        val virksomhetsToken = runBlocking { idPortenService.requestToken() }

        val headers = setIntegrasjonHeaders("Bearer ${virksomhetsToken.token}")

        try {
            val response = restTemplate.exchange("$baseUrl/digisos/api/v1/nav/kommune/$kommunenummer", HttpMethod.GET, HttpEntity<Nothing>(headers), KommuneInfo::class.java)

            return response.body!!

        } catch (e: HttpStatusCodeException) {
            log.warn("Fiks - hentKommuneInfo feilet - ${e.statusCode} ${e.statusText}", e)
            throw FiksException(e.statusCode, e.message, e)
        } catch (e: Exception) {
            log.warn("Fiks - hentKommuneInfo feilet", e)
            throw FiksException(null, e.message, e)
        }
    }

    private fun createHttpEntityOfString(body: String, name: String): HttpEntity<Any> {
        return createHttpEntity(body, name, null, "text/plain;charset=UTF-8")
    }

    private fun createHttpEntity(body: Any, name: String, filename: String?, contentType: String): HttpEntity<Any> {
        val headerMap = LinkedMultiValueMap<String, String>()
        val builder: ContentDisposition.Builder = ContentDisposition
                .builder("form-data")
                .name(name)
        val contentDisposition: ContentDisposition = if (filename == null) builder.build() else builder.filename(filename).build()

        headerMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        headerMap.add(HttpHeaders.CONTENT_TYPE, contentType)
        return HttpEntity(body, headerMap)
    }

    private fun serialiser(@NonNull metadata: Any): String {
        try {
            return objectMapper.writeValueAsString(metadata)
        } catch (e: JsonProcessingException) {
            throw RuntimeException("Feil under serialisering av metadata", e)
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
}

data class VedleggMetadata(
        val filnavn: String?,
        val mimetype: String?,
        val storrelse: Long
)