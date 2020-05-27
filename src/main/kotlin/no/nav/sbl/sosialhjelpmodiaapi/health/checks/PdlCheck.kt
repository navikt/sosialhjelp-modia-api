package no.nav.sbl.sosialhjelpmodiaapi.health.checks

import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.PdlClient
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.DependencyCheck
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.DependencyType
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.Importance
import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException

@Profile("!mock")
@Component
class PdlCheck(
        clientProperties: ClientProperties,
        private val pdlClient: PdlClient
) : DependencyCheck(
        DependencyType.REST,
        "PDL",
        clientProperties.pdlEndpointUrl,
        Importance.WARNING
) {
    override fun doCheck() {
        try {
            pdlClient.ping()
        } catch (e: RestClientException) {
            log.warn("Selftest - PDL - noe feilet", e)
            throw e
        }
    }

    companion object {
        private val log by logger()
    }
}
