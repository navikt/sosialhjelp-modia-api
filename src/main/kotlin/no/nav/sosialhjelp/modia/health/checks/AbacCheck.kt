package no.nav.sosialhjelp.modia.health.checks

import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.service.tilgangskontroll.AbacService
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("!(mock | mock-alt)")
@Component
class AbacCheck(
    private val abacService: AbacService,
    clientProperties: ClientProperties
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "ABAC"
    override val address = clientProperties.abacPdpEndpointUrl
    override val importance = Importance.WARNING

    override fun doCheck() {
        abacService.ping()
    }
}
