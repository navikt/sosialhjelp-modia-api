package no.nav.sosialhjelp.modia.config

import no.nav.sosialhjelp.modia.utils.getUnproxiedReactorClientHttpConnector
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import java.util.Base64

@Configuration
class WebClientConfig {

    @Bean
    fun webClient(webClientBuilder: WebClient.Builder): WebClient =
        webClientBuilder
            .clientConnector(getUnproxiedReactorClientHttpConnector())
            .build()

    @Bean
    @Profile("!local")
    fun basicAuthWebClient(webClientBuilder: WebClient.Builder): WebClient =
        webClientBuilder
            .clientConnector(getUnproxiedReactorClientHttpConnector())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic ${credentials()}")
            .build()

    companion object {
        private const val SRVSOSIALHJELP_MODIA_API_USERNAME: String = "SRVSOSIALHJELP_MODIA_API_USERNAME"
        private const val SRVSOSIALHJELP_MODIA_API_PASSWORD: String = "SRVSOSIALHJELP_MODIA_API_PASSWORD"

        private fun credentials(): String =
            Base64.getEncoder()
                .encodeToString(
                    "${System.getenv(SRVSOSIALHJELP_MODIA_API_USERNAME)}:${System.getenv(SRVSOSIALHJELP_MODIA_API_PASSWORD)}"
                        .toByteArray(Charsets.UTF_8)
                )
    }
}
