package no.nav.sosialhjelp.modia.health.checks

import no.nav.sosialhjelp.modia.client.sts.STSClient
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("!(mock | local)")
@Component
class STSCheck(
    clientProperties: ClientProperties,
    private val stsClient: STSClient
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "STS"
    override val address = clientProperties.stsTokenEndpointUrl
    override val importance = Importance.WARNING

    override fun doCheck() {
        stsClient.ping()
    }

    companion object {
        private val log by logger()
    }
}
