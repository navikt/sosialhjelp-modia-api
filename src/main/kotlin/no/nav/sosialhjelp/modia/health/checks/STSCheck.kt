package no.nav.sosialhjelp.modia.health.checks

import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import org.springframework.context.annotation.Profile
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
    override val address = clientProperties.stsConfigEndpointUrl
    override val importance = Importance.WARNING

    override fun doCheck() {
        try {
            restTemplate.exchange(address, HttpMethod.GET, null, String::class.java)
        } catch (e: RestClientException) {
            log.warn("Selftest - STS - noe feilet", e)
            throw e
        }
    }

    companion object {
        private val log by logger()
    }
}
