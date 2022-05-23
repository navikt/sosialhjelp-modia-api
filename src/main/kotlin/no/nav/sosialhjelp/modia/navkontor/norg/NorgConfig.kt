package no.nav.sosialhjelp.modia.navkontor.norg

import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.app.client.unproxiedHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class NorgConfig(
    private val webClientBuilder: WebClient.Builder,
    private val clientProperties: ClientProperties,
) {

    @Bean
    fun norgWebClient(): WebClient = webClientBuilder
        .clientConnector(ReactorClientHttpConnector(unproxiedHttpClient()))
        .codecs {
            it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
        }
        .baseUrl(clientProperties.norgEndpointUrl)
        .build()
}
