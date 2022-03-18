package no.nav.sosialhjelp.modia.health.checks

import no.nav.sosialhjelp.modia.client.fiks.KommuneInfoClient
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import org.springframework.stereotype.Component

@Component
class FiksCheck(
    clientProperties: ClientProperties,
    private val kommuneInfoClient: KommuneInfoClient
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "Fiks Digisos API"
    override val address = clientProperties.fiksDigisosEndpointUrl
    override val importance = Importance.WARNING

    override fun doCheck() {
        kommuneInfoClient.getAll()
    }
}
