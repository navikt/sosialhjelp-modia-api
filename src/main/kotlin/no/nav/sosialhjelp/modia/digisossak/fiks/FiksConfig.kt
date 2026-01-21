package no.nav.sosialhjelp.modia.digisossak.fiks

import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.utils.configureBuilder
import no.nav.sosialhjelp.modia.utils.sosialhjelpJsonMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.http.codec.json.JacksonJsonEncoder
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
class FiksConfig(
    private val webClientBuilder: WebClient.Builder,
    private val proxiedHttpClient: HttpClient,
    private val clientProperties: ClientProperties,
) {
    @Bean
    fun fiksWebClient(): WebClient =
        webClientBuilder
            .configureBuilder(proxiedHttpClient)
            .codecs {
                it.defaultCodecs().jacksonJsonDecoder(JacksonJsonDecoder(sosialhjelpJsonMapper))
                it.defaultCodecs().jacksonJsonEncoder(JacksonJsonEncoder(sosialhjelpJsonMapper))
            }
            .baseUrl(clientProperties.fiksDigisosEndpointUrl)
            .build()
}
