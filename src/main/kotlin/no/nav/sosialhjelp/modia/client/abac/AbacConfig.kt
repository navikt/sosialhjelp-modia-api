package no.nav.sosialhjelp.modia.client.abac

import com.google.common.net.HttpHeaders
import no.nav.sosialhjelp.modia.config.ClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@Profile("!(mock | local)")
class AbacConfig(
    private val basicAuthWebClient: WebClient,
    private val clientProperties: ClientProperties
) {

    @Bean
    fun abacWebClient(): WebClient {
        return basicAuthWebClient.mutate()
            .baseUrl(clientProperties.abacPdpEndpointUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_XACML)
            .build()
    }

    companion object {
        private const val MEDIA_TYPE_XACML = "application/xacml+json"
    }
}
