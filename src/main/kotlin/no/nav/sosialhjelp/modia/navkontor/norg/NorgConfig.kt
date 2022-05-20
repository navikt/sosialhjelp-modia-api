package no.nav.sosialhjelp.modia.navkontor.norg

import no.nav.sosialhjelp.modia.app.client.ClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class NorgConfig(
    private val nonProxiedWebClientBuilder: WebClient.Builder,
    private val clientProperties: ClientProperties,
) {

    @Bean
    fun norgWebClient(): WebClient = nonProxiedWebClientBuilder
        .baseUrl(clientProperties.norgEndpointUrl)
        .build()
}
