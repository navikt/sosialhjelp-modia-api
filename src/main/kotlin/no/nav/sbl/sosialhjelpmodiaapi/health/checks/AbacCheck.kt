package no.nav.sbl.sosialhjelpmodiaapi.health.checks

import no.nav.sbl.sosialhjelpmodiaapi.service.tilgangskontroll.AbacService
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.DependencyCheck
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.DependencyType
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.Importance
import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.springframework.stereotype.Component


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

    companion object {
        val log by logger()
    }
}