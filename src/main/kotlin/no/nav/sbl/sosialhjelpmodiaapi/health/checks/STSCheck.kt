package no.nav.sbl.sosialhjelpmodiaapi.health.checks

import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.DependencyCheck
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.DependencyType
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.Importance
import no.nav.sbl.sosialhjelpmodiaapi.logger
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
) : DependencyCheck(
        DependencyType.REST,
        "STS",
        clientProperties.stsTokenEndpointUrl,
        Importance.WARNING
) {
    override fun doCheck() {
        try {
            val requestUrl = "$address/.well-known/openid-configuration"
            restTemplate.exchange(requestUrl, HttpMethod.GET, null, String::class.java)
        } catch (e: RestClientException) {
            log.warn("Selftest - STS - noe feilet", e)
            throw e
        }
    }

    companion object {
        private val log by logger()
    }
}
