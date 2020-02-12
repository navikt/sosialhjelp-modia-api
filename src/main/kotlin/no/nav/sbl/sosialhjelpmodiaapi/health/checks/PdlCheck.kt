package no.nav.sbl.sosialhjelpmodiaapi.health.checks

import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.AbstractDependencyCheck
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.DependencyType
import no.nav.sbl.sosialhjelpmodiaapi.health.selftest.Importance
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.pdl.PdlClient
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException

@Component
class PdlCheck (clientProperties: ClientProperties,
                private val pdlClient: PdlClient) : AbstractDependencyCheck(
        DependencyType.REST,
        "PDL",
        clientProperties.pdlEndpointUrl,
        Importance.WARNING
) {
    companion object {
        val log by logger()
    }

    override fun doCheck() {
        try {
            pdlClient.ping()
        } catch (e: RestClientException) {
            log.warn("Selftest - PDL - noe feilet", e)
            throw e
        }
    }
}