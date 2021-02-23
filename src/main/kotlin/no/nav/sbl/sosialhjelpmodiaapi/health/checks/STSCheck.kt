package no.nav.sbl.sosialhjelpmodiaapi.health.checks

import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.forwardHeaders
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Profile("!mock")
@Component
class STSCheck(
    clientProperties: ClientProperties,
    private val restTemplate: RestTemplate
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "STS"
    override val address = clientProperties.stsTokenEndpointUrl
    override val importance = Importance.WARNING

    override fun doCheck() {
        try {
            val requestUrl = "$address/.well-known/openid-configuration"
            restTemplate.exchange(requestUrl, HttpMethod.GET, HttpEntity<Nothing>(forwardHeaders()), String::class.java)
        } catch (e: RestClientException) {
            log.warn("Selftest - STS - noe feilet", e)
            throw e
        }
    }

    companion object {
        private val log by logger()
    }
}
