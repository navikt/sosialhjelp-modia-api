package no.nav.sbl.sosialhjelpmodiaapi.abac

import no.nav.abac.xacml.NavAttributter
import no.nav.sbl.sosialhjelpmodiaapi.common.TilgangskontrollException
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.logging.AuditService
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

interface AbacClient {

    fun sjekkTilgang(request: Request): AbacResponse

}

@Profile("!(mock | local)")
@Component
class AbacClientImpl(
        clientProperties: ClientProperties,
        private val serviceuserBasicAuthRestTemplate: RestTemplate,
        private val auditService: AuditService
) : AbacClient {

    private val url = clientProperties.abacPdpEndpointUrl

    override fun sjekkTilgang(request: Request): AbacResponse {

        val postingString = XacmlMapper.mapRequestToEntity(XacmlRequest(request))
        val requestEntity = HttpEntity(postingString, headers())

        val responseBody = try {
            val response = serviceuserBasicAuthRestTemplate.exchange(url, HttpMethod.POST, requestEntity, String::class.java)
            response.body!!
        } catch (e: HttpStatusCodeException) {
            log.error("Abac - noe feilet. Status: ${e.statusCode}, message: ${e.message}.", e)
            throw TilgangskontrollException("Noe feilet ved kall til Abac.", e)
        } catch (e: Exception) {
            log.error("Abac - noe feilet.", e)
            throw TilgangskontrollException("Noe feilet ved kall til Abac.", e)
        }

        val xacmlResponse = XacmlMapper.mapRawResponse(responseBody)
        val abacResponse: AbacResponse = xacmlResponse.response[0]

        // auditlogg abac-kall
        auditService.reportAbac(request.fnr, url, HttpMethod.POST, abacResponse)

        return abacResponse
    }

    private fun headers(): HttpHeaders {
        val headers = HttpHeaders()
        headers.set("Content-Type", MEDIA_TYPE)
        return headers
    }

    private val Request.fnr: String
        get() {
            return resource?.attributes?.first { it.attributeId == NavAttributter.RESOURCE_FELLES_PERSON_FNR }?.value!!
        }

    companion object {
        private val log by logger()

        private const val MEDIA_TYPE = "application/xacml+json"
    }
}
