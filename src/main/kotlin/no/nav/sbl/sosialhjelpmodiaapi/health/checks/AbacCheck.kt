package no.nav.sbl.sosialhjelpmodiaapi.health.checks

import no.nav.sbl.sosialhjelpmodiaapi.abac.AbacService
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.AbstractDependencyCheck
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.DependencyType
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.Importance
import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component


@Profile("!mock")
@Component
class AbacCheck(
        private val abacService: AbacService,
        clientProperties: ClientProperties
) : AbstractDependencyCheck(
        DependencyType.REST,
        "ABAC",
        clientProperties.abacPdpEndpointUrl,
        Importance.WARNING
) {
    override fun doCheck() {
        abacService.ping()
    }

    companion object {
        val log by logger()
    }
}