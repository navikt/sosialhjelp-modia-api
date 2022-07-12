package no.nav.sosialhjelp.modia.app.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import reactor.netty.http.client.HttpClient

@Profile("!(dev|mock-alt|local)")
@Configuration
class ProxiedHttpClientConfig {

    @Value("\${HTTPS_PROXY}")
    private lateinit var proxyUrl: String

    @Bean
    fun proxiedHttpClient(): HttpClient = proxiedHttpClient(proxyUrl)
}

@Profile("dev|mock-alt|local")
@Configuration
class MockProxiedHttpClientConfig {

    @Bean
    fun proxiedHttpClient(): HttpClient = unproxiedHttpClient()
}
