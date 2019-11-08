package no.nav.sbl.sosialhjelpmodiaapi.abac

import no.nav.abac.xacml.StandardAttributter.ACTION_ID
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.resolveSrvPassword
import no.nav.sbl.sosialhjelpmodiaapi.resolveSrvUser
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

@Component
class AbacClient(clientProperties: ClientProperties,
                 private val restTemplate: RestTemplate) {

    private val url = clientProperties.abacPdpEndpointUrl

    fun sjekkTilgang(request: Request): Decision {
        //logg request-info til auditlogger

        val xacmlResponse = askForPermission(XacmlRequest(request))
        val decision = xacmlResponse.response.decision

        //logg response-info til auditlogger
        return decision
    }

    fun ping(): Decision {
        // lag request som blir riktig ref https://confluence.adeo.no/display/ABAC/PDP+ping ->

        val pingAttribute = Attribute(ACTION_ID, "ping")
        val request = Request(null, Attributes(mutableListOf(pingAttribute)), null, null)

        val xacmlResponse = askForPermission(XacmlRequest(request))
        return xacmlResponse.response.decision
    }

    private fun askForPermission(request: XacmlRequest): XacmlResponse { // flagg for useCache?
        val postingString = XacmlMapper.mapRequestToEntity(request)
        val content = request(postingString)
        return XacmlMapper.mapRawResponse(content)
    }

    private fun request(postingString: String): String {
        val requestEntity = HttpEntity(postingString, headers())
        try {
            val response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String::class.java)

            return response.body!!
        } catch(e: HttpStatusCodeException) {
            log.warn("Abac response: ${e.responseBodyAsString}")
            log.error("Abac - noe feilet - ${e.statusCode} ${e.message}", e)
            throw RuntimeException("Noe feilet ved kall til Abac", e)
        } catch (e: Exception) {
            log.error("Abac - noe feilet", e)
            throw RuntimeException("Noe feilet ved kall til Abac", e)
        }
    }

    private fun headers(): HttpHeaders {
        val headers = HttpHeaders()
        headers.set("Content-Type", MEDIA_TYPE)
        headers.setBasicAuth(resolveSrvUser(), resolveSrvPassword())
        return headers
    }

    companion object {
        private val log by logger()

        private val auditLog = getLogger("AuditLogger")

        private const val MEDIA_TYPE = "application/xacml+json"
    }

}