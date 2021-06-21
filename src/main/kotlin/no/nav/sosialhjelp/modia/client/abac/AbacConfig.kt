package no.nav.sosialhjelp.modia.client.abac

import com.google.common.net.HttpHeaders
import no.nav.sosialhjelp.modia.config.ClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient
import java.util.Base64

@Configuration
@Profile("!(mock | local)")
class AbacConfig(
    private val webClient: WebClient,
    private val clientProperties: ClientProperties
) {

    @Bean
    fun abacWebClient(): WebClient {
        return webClient.mutate()
            .baseUrl(clientProperties.abacPdpEndpointUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic ${credentials()}")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_XACML)
            .build()
    }

    companion object {
        private const val SRVSOSIALHJELP_MODIA_API_USERNAME: String = "SRVSOSIALHJELP_MODIA_API_USERNAME"
        private const val SRVSOSIALHJELP_MODIA_API_PASSWORD: String = "SRVSOSIALHJELP_MODIA_API_PASSWORD"

        private const val MEDIA_TYPE_XACML = "application/xacml+json"

        private fun credentials(): String {
            return Base64.getEncoder().encodeToString("${System.getenv(SRVSOSIALHJELP_MODIA_API_USERNAME)}:${System.getenv(SRVSOSIALHJELP_MODIA_API_PASSWORD)}".toByteArray(Charsets.UTF_8))
        }
    }
}
