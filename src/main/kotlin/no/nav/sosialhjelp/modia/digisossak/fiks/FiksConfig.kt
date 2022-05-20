package no.nav.sosialhjelp.modia.digisossak.fiks

import no.nav.sosialhjelp.modia.app.client.ClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class FiksConfig(
    private val proxiedWebClientBuilder: WebClient.Builder,
    private val clientProperties: ClientProperties,
) {

    @Bean
    fun fiksWebClient(): WebClient =
        proxiedWebClientBuilder
            .baseUrl(clientProperties.fiksDigisosEndpointUrl)
            .build()
}
