package no.nav.sosialhjelp.modia.client.fiks

import no.nav.sosialhjelp.client.kommuneinfo.FiksProperties
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClientImpl
import no.nav.sosialhjelp.modia.config.ClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class KommuneInfoClientConfig(
    private val proxiedWebClient: WebClient,
    private val clientProperties: ClientProperties
) {

    @Bean
    fun kommuneInfoClient(): KommuneInfoClient {
        return KommuneInfoClientImpl(
            proxiedWebClient,
            fiksProperties()
        )
    }

    private fun fiksProperties(): FiksProperties {
        return FiksProperties(
            clientProperties.fiksDigisosEndpointUrl + FiksPaths.PATH_KOMMUNEINFO,
            clientProperties.fiksDigisosEndpointUrl + FiksPaths.PATH_ALLE_KOMMUNEINFO,
            clientProperties.fiksIntegrasjonId,
            clientProperties.fiksIntegrasjonpassord
        )
    }
}
