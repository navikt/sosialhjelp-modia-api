package no.nav.sosialhjelp.modia.health.checks

import no.nav.sosialhjelp.modia.client.pdl.PdlClient
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException

@Component
class PdlCheck(
    clientProperties: ClientProperties,
    private val pdlClient: PdlClient
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "PDL"
    override val address = clientProperties.pdlEndpointUrl
    override val importance = Importance.WARNING

    override fun doCheck() {
        try {
            pdlClient.ping()
        } catch (e: RestClientException) {
            log.warn("Selftest - PDL - noe feilet", e)
            throw e
        }
    }

    companion object {
        private val log by logger()
    }
}
