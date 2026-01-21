package no.nav.sosialhjelp.modia.app.maskinporten

import no.nav.sosialhjelp.modia.auth.texas.TexasClient
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.utils.MiljoUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient

@Configuration
class MaskinportenClientConfig(
    @Value("\${maskinporten_clientid}") private val clientId: String,
    @Value("\${maskinporten_scopes}") private val scopes: String,
    @Value("\${maskinporten_well_known_url}") private val wellKnownUrl: String,
    @Value("\${maskinporten_client_jwk}") private val clientJwk: String,
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient,
    private val miljoUtils: MiljoUtils,
    private val texasClient: TexasClient,
) {
    @Bean
    @Profile("!test&!gcp")
    fun maskinportenClientFss(): MaskinportenClient =
        MaskinportenClientFss(maskinPortenWebClient, maskinportenProperties, wellknown, miljoUtils)

    @Bean
    @Profile("!test&gcp")
    fun maskinportenClientGcp(): MaskinportenClient = MaskinportenClientGcp(texasClient)

    @Bean
    @Profile("test")
    fun maskinportenClientTest(): MaskinportenClientFss =
        MaskinportenClientFss(
            maskinPortenWebClient,
            maskinportenProperties,
            WellKnown("iss", "token_url"),
            miljoUtils,
        )

    private val maskinPortenWebClient: WebClient =
        webClientBuilder
            .clientConnector(ReactorClientHttpConnector(proxiedHttpClient))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }.build()

    private val maskinportenProperties =
        MaskinportenProperties(
            clientId = clientId,
            clientJwk = clientJwk,
            scope = scopes,
            wellKnownUrl = wellKnownUrl,
        )

    private val wellknown: WellKnown
        get() =
            maskinPortenWebClient
                .get()
                .uri(wellKnownUrl)
                .retrieve()
                .bodyToMono<WellKnown>()
                .doOnSuccess { log.info("Hentet WellKnown for Maskinporten") }
                .doOnError { log.warn("Feil ved henting av WellKnown for Maskinporten", it) }
                .block() ?: throw RuntimeException("Feil ved henting av WellKnown for Maskinporten")

    companion object {
        private val log by logger()
    }
}

data class WellKnown(
    val issuer: String,
    val token_endpoint: String,
)

data class MaskinportenProperties(
    val clientId: String,
    val clientJwk: String,
    val scope: String,
    val wellKnownUrl: String,
)
