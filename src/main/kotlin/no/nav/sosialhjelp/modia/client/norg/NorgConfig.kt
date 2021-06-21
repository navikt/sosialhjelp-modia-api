package no.nav.sosialhjelp.modia.client.norg

import no.nav.sosialhjelp.modia.config.ClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class NorgConfig(
    private val webClient: WebClient,
    private val clientProperties: ClientProperties,
) {

    @Bean
    fun norgWebClient(): WebClient = webClient.mutate()
        .baseUrl(clientProperties.norgEndpointUrl)
        .build()
}
