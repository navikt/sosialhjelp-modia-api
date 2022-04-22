package no.nav.sosialhjelp.modia.app.health.checks

import no.nav.sosialhjelp.modia.common.NorgException
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_CALL_ID
import no.nav.sosialhjelp.modia.utils.mdc.MDCUtils.getCallId
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class NorgCheck(
    private val norgWebClient: WebClient,
    clientProperties: ClientProperties
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "NORG2"
    override val address = clientProperties.norgEndpointUrl
    override val importance = Importance.WARNING

    override fun doCheck() {
        norgWebClient.get()
            .uri("/kodeverk/EnhetstyperNorg")
            .header(HEADER_CALL_ID, getCallId())
            .retrieve()
            .bodyToMono<String>()
            .onErrorMap { e ->
                when (e) {
                    is WebClientResponseException -> log.warn("Selftest - Norg2 - Noe feilet - ${e.statusCode} ${e.statusText}", e)
                    else -> log.warn("Selftest - Norg2 - Noe feilet", e)
                }
                NorgException(e.message, e)
            }
            .block()
    }

    companion object {
        private val log by logger()
    }
}
