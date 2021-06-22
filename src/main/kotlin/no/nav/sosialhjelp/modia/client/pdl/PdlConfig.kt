package no.nav.sosialhjelp.modia.client.pdl

import no.nav.sosialhjelp.modia.config.ClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Profile("!(mock | local)")
@Configuration
class PdlConfig(
    private val webClient: WebClient,
    private val clientProperties: ClientProperties
) {

    @Bean
    fun pdlWebClient(): WebClient =
        webClient.mutate()
            .baseUrl(clientProperties.pdlEndpointUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
}
