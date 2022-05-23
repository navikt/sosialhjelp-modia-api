package no.nav.sosialhjelp.modia.person.pdl

import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.app.client.unproxiedHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient

@Profile("!local")
@Configuration
class PdlConfig(
    private val webClientBuilder: WebClient.Builder,
    private val clientProperties: ClientProperties
) {

    @Bean
    fun pdlWebClient(): WebClient =
        webClientBuilder
            .clientConnector(ReactorClientHttpConnector(unproxiedHttpClient()))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }
            .baseUrl(clientProperties.pdlEndpointUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
}
