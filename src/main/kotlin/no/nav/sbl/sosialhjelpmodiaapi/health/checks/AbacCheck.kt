package no.nav.sbl.sosialhjelpmodiaapi.health.checks

import no.nav.sbl.sosialhjelpmodiaapi.abac.AbacClient
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.AbstractDependencyCheck
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.DependencyType
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.Importance
import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.springframework.stereotype.Component


@Component
class AbacCheck(private val abacClient: AbacClient,
                clientProperties: ClientProperties) : AbstractDependencyCheck(
        DependencyType.REST,
        "ABAC",
        clientProperties.abacPdpEndpointUrl,
        Importance.WARNING
) {
    companion object {
        val log by logger()
    }

    override fun doCheck() {
        abacClient.ping()
    }
}