package no.nav.sbl.sosialhjelpmodiaapi.client.norg

import no.nav.sbl.sosialhjelpmodiaapi.common.NorgException
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.domain.NavEnhet
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.HEADER_CALL_ID
import no.nav.sbl.sosialhjelpmodiaapi.utils.mdc.MDCUtils.getCallId
import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate


@Profile("!mock & !local")
@Component
class NorgClientImpl(
        clientProperties: ClientProperties,
        private val restTemplate: RestTemplate
) : NorgClient {

    private val baseUrl = clientProperties.norgEndpointUrl

    override fun hentNavEnhet(enhetsnr: String): NavEnhet {
        try {
            log.info("Norg2 - GET enhet $enhetsnr")
            val urlTemplate = "$baseUrl/enhet/{enhetsnr}"
            val vars = mapOf("enhetsnr" to enhetsnr)
            val requestEntity = createRequestEntity()
            val response = restTemplate.exchange(urlTemplate, HttpMethod.GET, requestEntity, String::class.java, vars)

            log.info("Norg2 - GET enhet OK")
            return objectMapper.readValue(response.body!!, NavEnhet::class.java)

        } catch (e: HttpStatusCodeException) {
            log.warn("Norg2 - Noe feilet - ${e.statusCode} ${e.statusText}", e)
            throw NorgException(e.statusCode, e.message, e)
        } catch (e: Exception) {
            log.warn("Norg2 - Noe feilet", e)
            throw NorgException(null, e.message, e)
        }
    }

    private fun createRequestEntity(): HttpEntity<Nothing> {
        val headers = HttpHeaders()
        headers.set(HEADER_CALL_ID, getCallId())
        return HttpEntity(headers)
    }

    companion object {
        private val log by logger()
    }
}
