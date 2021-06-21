package no.nav.sosialhjelp.modia.config

import no.nav.sosialhjelp.modia.utils.getUnproxiedReactorClientHttpConnector
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean
    fun webClient(webClientBuilder: WebClient.Builder): WebClient =
        webClientBuilder
            .clientConnector(getUnproxiedReactorClientHttpConnector())
            .build()
}
