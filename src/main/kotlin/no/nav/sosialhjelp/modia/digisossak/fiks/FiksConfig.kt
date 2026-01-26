package no.nav.sosialhjelp.modia.digisossak.fiks

import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.utils.sosialhjelpJsonMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.http.codec.json.JacksonJsonEncoder
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class FiksConfig(
    private val webClientBuilder: WebClient.Builder,
    private val clientProperties: ClientProperties,
) {
    @Bean
    fun fiksWebClient(): WebClient =
        webClientBuilder
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
                it.defaultCodecs().jacksonJsonDecoder(JacksonJsonDecoder(sosialhjelpJsonMapper))
                it.defaultCodecs().jacksonJsonEncoder(JacksonJsonEncoder(sosialhjelpJsonMapper))
            }.baseUrl(clientProperties.fiksDigisosEndpointUrl)
            .build()
}
