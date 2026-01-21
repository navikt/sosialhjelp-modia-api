package no.nav.sosialhjelp.modia.tilgang.azure

import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.app.exceptions.ManglendeTilgangException
import no.nav.sosialhjelp.modia.app.maskinporten.WellKnown
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.utils.configureWebClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

class AzuredingsWebConfig(
    val tokenEndpoint: String,
)

@Configuration
class AzuredingsClientConfig(
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient,
    private val clientProperties: ClientProperties,
) {
    @Bean
    @Profile("!test")
    fun azuredingsWebClient(): AzuredingsWebConfig {
        val wellKnown = downloadWellKnown(clientProperties.azuredingsUrl)
        log.info(
            "AzuredingsClient: Lastet ned well known fra: ${clientProperties.azuredingsUrl} bruker token endpoint: ${wellKnown.token_endpoint}",
        )
        return AzuredingsWebConfig(wellKnown.token_endpoint)
    }

    private val azuredingsWebClient: WebClient = webClientBuilder.configureWebClient(proxiedHttpClient)

    fun downloadWellKnown(url: String): WellKnown =
        azuredingsWebClient
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono(WellKnown::class.java)
            .block()
            ?: throw ManglendeTilgangException("Feiler under henting av well-known konfigurasjon fra $url")

    @Bean
    @Profile("test")
    fun azuredingsWebClientTest(): AzuredingsWebConfig {
        log.info("AzuredingsClient: Setter opp test client som bruker token endpoint: ${clientProperties.azuredingsUrl}")
        return AzuredingsWebConfig(clientProperties.azuredingsUrl)
    }

    companion object {
        private val log by logger()
    }
}
