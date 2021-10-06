package no.nav.sosialhjelp.modia.client.sts

import no.nav.sosialhjelp.modia.config.ClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Profile("!local")
@Configuration
class StsConfig(
    private val clientProperties: ClientProperties,
    private val basicAuthWebClient: WebClient
) {

    @Bean
    fun stsWebClient(): WebClient =
        basicAuthWebClient.mutate()
            .baseUrl(clientProperties.stsTokenEndpointUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build()
}
