package no.nav.sbl.sosialhjelpmodiaapi.abac

import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

@Component
class AbacClient(
        clientProperties: ClientProperties,
        private val serviceuserBasicAuthRestTemplate: RestTemplate
) {

    private val url = clientProperties.abacPdpEndpointUrl

    fun sjekkTilgang(request: Request): AbacResponse {
        //logg request-info til auditlogger

        val postingString = XacmlMapper.mapRequestToEntity(XacmlRequest(request))
        val requestEntity = HttpEntity(postingString, headers())

        val responseBody = try {
            val response = serviceuserBasicAuthRestTemplate.exchange(url, HttpMethod.POST, requestEntity, String::class.java)
            response.body!!
        } catch (e: HttpStatusCodeException) {
            log.warn("Abac - feil, response: ${e.responseBodyAsString}")
            log.error("Abac - noe feilet - ${e.statusCode} ${e.message}", e)
            throw RuntimeException("Noe feilet ved kall til Abac", e)
        } catch (e: Exception) {
            log.error("Abac - noe feilet", e)
            throw RuntimeException("Noe feilet ved kall til Abac", e)
        }

        val xacmlResponse = XacmlMapper.mapRawResponse(responseBody)

        //logg response-info til auditlogger
        return xacmlResponse.response[0]
    }

    private fun headers(): HttpHeaders {
        val headers = HttpHeaders()
        headers.set("Content-Type", MEDIA_TYPE)
        return headers
    }

    companion object {
        private val log by logger()

        private val auditLog = getLogger("AuditLogger")

        private const val MEDIA_TYPE = "application/xacml+json"
    }

}