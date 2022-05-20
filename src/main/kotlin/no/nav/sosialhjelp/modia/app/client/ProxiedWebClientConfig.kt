package no.nav.sosialhjelp.modia.app.client

import no.nav.sosialhjelp.modia.utils.objectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Profile("!(mock-alt|local)")
@Configuration
class ProxiedWebClientConfig {

    @Value("\${HTTPS_PROXY}")
    private lateinit var proxyUrl: String

    @Bean
    fun proxiedWebClientBuilder(): WebClient.Builder =
        WebClient.builder()
            .clientConnector(getProxiedReactorClientHttpConnector(proxyUrl))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
                it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
                it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
            }

    @Bean
    fun proxiedHttpClient(): HttpClient = proxiedHttpClient(proxyUrl)
}

@Profile("mock-alt|local")
@Configuration
class MockProxiedWebClientConfig {

    @Bean
    fun proxiedWebClientBuilder(): WebClient.Builder =
        WebClient.builder()
            .clientConnector(getUnproxiedReactorClientHttpConnector())
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }

    @Bean
    fun proxiedHttpClient(): HttpClient = unproxiedHttpClient()
}

@Configuration
class NonProxiedWebClientConfig {

    @Bean
    fun nonProxiedWebClientBuilder(): WebClient.Builder =
        WebClient.builder()
            .clientConnector(getUnproxiedReactorClientHttpConnector())
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }
}
