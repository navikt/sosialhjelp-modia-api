package no.nav.sosialhjelp.modia.client.azure

import no.nav.sosialhjelp.modia.client.maskinporten.WellKnown
import no.nav.sosialhjelp.modia.common.ManglendeTilgangException
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient

class AzuredingsWebClient(val webClient: WebClient)

@Configuration
class AzuredingsClientConfig(
    private val clientProperties: ClientProperties,
) {
    @Bean
    @Profile("!test")
    fun azuredingsWebClient(webClientBuilder: WebClient.Builder): AzuredingsWebClient {
        val wellKnown = downloadWellKnown(clientProperties.azuredingsUrl)
        log.info("AzuredingsClient: Lastet ned well known fra: ${clientProperties.azuredingsUrl} bruker token endpoint: ${wellKnown.token_endpoint}")
        return AzuredingsWebClient(
            buildWebClient(webClientBuilder, wellKnown.token_endpoint, applicationFormUrlencodedHeaders())
        )
    }

    fun downloadWellKnown(url: String): WellKnown =
        WebClient.create()
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono(WellKnown::class.java)
            .block()
            ?: throw ManglendeTilgangException("Feiler under henting av well-known konfigurasjon fra $url")

    @Bean
    @Profile("test")
    fun azuredingsWebClientTest(webClientBuilder: WebClient.Builder): AzuredingsWebClient {
        log.info("AzuredingsClient: Setter opp test client som bruker token endpoint: ${clientProperties.azuredingsUrl}")
        return AzuredingsWebClient(
            buildWebClient(webClientBuilder, clientProperties.azuredingsUrl)
        )
    }

    companion object {
        private val log by logger()
    }
}
