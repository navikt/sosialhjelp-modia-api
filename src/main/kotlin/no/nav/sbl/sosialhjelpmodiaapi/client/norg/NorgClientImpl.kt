package no.nav.sbl.sosialhjelpmodiaapi.client.norg

import no.nav.sbl.sosialhjelpmodiaapi.common.NorgException
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.domain.NavEnhet
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.HEADER_CALL_ID
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.forwardHeaders
import no.nav.sbl.sosialhjelpmodiaapi.utils.mdc.MDCUtils.getCallId
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
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

    override fun hentNavEnhet(enhetsnr: String): NavEnhet? {
        if (enhetsnr == "") return null

        try {
            log.info("Norg2 - GET enhet $enhetsnr")
            val urlTemplate = "$baseUrl/enhet/{enhetsnr}"
            val vars = mapOf("enhetsnr" to enhetsnr)
            val requestEntity = createRequestEntity()
            val response = restTemplate.exchange(urlTemplate, HttpMethod.GET, requestEntity, NavEnhet::class.java, vars)

            log.info("Norg2 - GET enhet OK")
            return response.body!!

        } catch (e: HttpStatusCodeException) {
            log.warn("Norg2 - Noe feilet - ${e.statusCode} ${e.statusText}", e)
            throw NorgException(e.message, e)
        } catch (e: Exception) {
            log.warn("Norg2 - Noe feilet", e)
            throw NorgException(e.message, e)
        }
    }

    private fun createRequestEntity(): HttpEntity<Nothing> {
        val headers = forwardHeaders()
        headers.set(HEADER_CALL_ID, getCallId())
        return HttpEntity(headers)
    }

    companion object {
        private val log by logger()
    }
}
