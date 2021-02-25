package no.nav.sosialhjelp.modia.health.checks

import no.nav.sosialhjelp.modia.common.NorgException
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_CALL_ID
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.forwardHeaders
import no.nav.sosialhjelp.modia.utils.mdc.MDCUtils.getCallId
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

@Profile("!mock")
@Component
class NorgCheck(
        private val restTemplate: RestTemplate,
        clientProperties: ClientProperties
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "NORG2"
    override val address = clientProperties.norgEndpointUrl
    override val importance = Importance.WARNING

    override fun doCheck() {
        try {
            val headers = forwardHeaders()
            headers.set(HEADER_CALL_ID, getCallId())

            // samme kall som selftest i soknad-api
            restTemplate.exchange("$address/kodeverk/EnhetstyperNorg", HttpMethod.GET, HttpEntity<Nothing>(headers), String::class.java)
        } catch (e: HttpStatusCodeException) {
            log.warn("Selftest - Norg2 - noe feilet - ${e.statusCode} ${e.statusText}", e)
            throw NorgException(e.message, e)
        } catch (e: Exception) {
            log.warn("Selftest - Norg2 - noe feilet", e)
            throw NorgException(e.message, e)
        }
    }

    companion object {
        private val log by logger()
    }
}