package no.nav.sosialhjelp.modia.app.health.checks

import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.navkontor.norg.NorgClient
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import org.springframework.stereotype.Component

@Component
class NorgCheck(
    private val norgClient: NorgClient,
    clientProperties: ClientProperties
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "NORG2"
    override val address = clientProperties.norgEndpointUrl
    override val importance = Importance.WARNING

    override fun doCheck() {
        try {
            norgClient.ping()
        } catch (e: Exception) {
            log.warn("Selftest - Norg2 - noe feilet", e)
            throw e
        }
    }

    companion object {
        private val log by logger()
    }
}
