package no.nav.sosialhjelp.modia.app.client

import no.nav.sosialhjelp.modia.utils.getProxiedReactorClientHttpConnector
import no.nav.sosialhjelp.modia.utils.getUnproxiedReactorClientHttpConnector
import no.nav.sosialhjelp.modia.utils.objectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.WebClient

@Profile("!(mock-alt|local)")
@Configuration
class ProxiedWebClientConfig {

    @Value("\${HTTPS_PROXY}")
    private lateinit var proxyUrl: String

    @Bean
    fun proxiedWebClient(webClientBuilder: WebClient.Builder): WebClient =
        webClientBuilder
            .clientConnector(getProxiedReactorClientHttpConnector(proxyUrl))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
                it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
                it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
            }
            .build()
}

@Profile("mock-alt|local")
@Configuration
class MockProxiedWebClientConfig {

    @Bean
    fun proxiedWebClient(webClientBuilder: WebClient.Builder): WebClient =
        webClientBuilder
            .clientConnector(getUnproxiedReactorClientHttpConnector())
            .build()
}

@Configuration
class UnproxiedWebClientConfig {

    @Bean
    fun webClient(webClientBuilder: WebClient.Builder): WebClient =
        webClientBuilder
            .clientConnector(getUnproxiedReactorClientHttpConnector())
            .build()
}
