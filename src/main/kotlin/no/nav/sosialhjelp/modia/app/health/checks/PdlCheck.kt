package no.nav.sosialhjelp.modia.app.health.checks

import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.person.pdl.PdlClient
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import org.springframework.stereotype.Component

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
        } catch (e: Exception) {
            log.warn("Selftest - PDL - noe feilet", e)
            throw e
        }
    }

    companion object {
        private val log by logger()
    }
}
