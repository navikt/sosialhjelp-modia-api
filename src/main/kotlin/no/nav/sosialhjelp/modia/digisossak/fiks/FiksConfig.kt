package no.nav.sosialhjelp.modia.digisossak.fiks

import no.nav.sosialhjelp.modia.utils.objectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
class FiksConfig(
    private val webClientBuilder: WebClient.Builder,
    private val proxiedHttpClient: HttpClient,
) {

    @Bean
    fun fiksWebClient(): WebClient =
        webClientBuilder
            .clientConnector(ReactorClientHttpConnector(proxiedHttpClient))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
                it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
                it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
            }
            .build()
}
