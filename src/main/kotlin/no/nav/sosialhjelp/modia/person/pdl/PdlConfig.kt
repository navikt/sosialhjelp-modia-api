package no.nav.sosialhjelp.modia.person.pdl

import no.nav.sosialhjelp.modia.app.client.ClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Profile("!local")
@Configuration
class PdlConfig(
    private val nonProxiedWebClientBuilder: WebClient.Builder,
    private val clientProperties: ClientProperties
) {

    @Bean
    fun pdlWebClient(): WebClient =
        nonProxiedWebClientBuilder
            .baseUrl(clientProperties.pdlEndpointUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
}
