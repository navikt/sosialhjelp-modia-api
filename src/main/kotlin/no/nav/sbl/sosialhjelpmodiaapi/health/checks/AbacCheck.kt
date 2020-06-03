package no.nav.sbl.sosialhjelpmodiaapi.health.checks

import no.nav.sbl.sosialhjelpmodiaapi.service.tilgangskontroll.AbacService
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import org.springframework.stereotype.Component


@Profile("!mock")
@Component
class AbacCheck(
        private val abacService: AbacService,
        clientProperties: ClientProperties
) : DependencyCheck(
        DependencyType.REST,
        "ABAC",
        clientProperties.abacPdpEndpointUrl,
        Importance.WARNING
) {
    override fun doCheck() {
        abacService.ping()
    }

}