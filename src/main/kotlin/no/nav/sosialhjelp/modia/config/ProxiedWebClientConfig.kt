package no.nav.sosialhjelp.modia.config

import no.nav.sosialhjelp.modia.utils.getProxiedReactorClientHttpConnector
import no.nav.sosialhjelp.modia.utils.getUnproxiedReactorClientHttpConnector
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient

@Profile("!(mock|mock-alt|local)")
@Configuration
class ProxiedWebClientConfig {

    @Value("\${HTTPS_PROXY}")
    private lateinit var proxyUrl: String

    @Bean
    fun proxiedWebClient(webClientBuilder: WebClient.Builder): WebClient =
        webClientBuilder
            .clientConnector(getProxiedReactorClientHttpConnector(proxyUrl))
            .build()
}

@Profile("mock|mock-alt|local")
@Configuration
class MockProxiedWebClientConfig {

    @Bean
    fun proxiedWebClient(webClientBuilder: WebClient.Builder): WebClient =
        webClientBuilder
            .clientConnector(getUnproxiedReactorClientHttpConnector())
            .build()
}
