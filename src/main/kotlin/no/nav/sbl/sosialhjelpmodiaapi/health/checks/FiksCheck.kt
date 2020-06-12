package no.nav.sbl.sosialhjelpmodiaapi.health.checks

import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.HentKommuneInfoClient
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("!mock")
@Component
class FiksCheck(
        clientProperties: ClientProperties,
        private val hentKommuneInfoClient: HentKommuneInfoClient
) : DependencyCheck(
        DependencyType.REST,
        "Fiks Digisos API",
        clientProperties.fiksDigisosEndpointUrl,
        Importance.WARNING
) {
    override fun doCheck() {
        hentKommuneInfoClient.hentAlleKommuneInfo()
    }
}
