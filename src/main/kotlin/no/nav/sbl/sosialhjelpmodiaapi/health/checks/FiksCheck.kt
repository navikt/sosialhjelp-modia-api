package no.nav.sbl.sosialhjelpmodiaapi.health.checks

import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.DependencyCheck
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.DependencyType
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.Importance
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("!mock")
@Component
class FiksCheck(
        clientProperties: ClientProperties,
        private val fiksClient: FiksClient
) : DependencyCheck(
        DependencyType.REST,
        "Fiks Digisos API",
        clientProperties.fiksDigisosEndpointUrl,
        Importance.WARNING
) {
    override fun doCheck() {
        // midlertidig settes kommunenummer lik Trondheim kommune
        fiksClient.hentKommuneInfo("5001")
    }
}
