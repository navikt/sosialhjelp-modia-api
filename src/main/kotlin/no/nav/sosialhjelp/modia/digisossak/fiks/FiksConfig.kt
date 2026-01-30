package no.nav.sosialhjelp.modia.digisossak.fiks

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.utils.sosialhjelpJsonMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.http.codec.json.JacksonJsonEncoder
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class FiksConfig(
    private val webClientBuilder: WebClient.Builder,
    private val clientProperties: ClientProperties,
) {
    @Bean
    fun fiksWebClient(): WebClient {
        val connectionProvider = ConnectionProvider.builder("fiks-connection-pool")
            .maxConnections(50)
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofMinutes(5))
            .evictInBackground(Duration.ofSeconds(30))
            .build()

        val httpClient = HttpClient.create(connectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(30, TimeUnit.SECONDS))
                    .addHandlerLast(WriteTimeoutHandler(30, TimeUnit.SECONDS))
            }

        return webClientBuilder
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
                it.defaultCodecs().jacksonJsonDecoder(JacksonJsonDecoder(sosialhjelpJsonMapper))
                it.defaultCodecs().jacksonJsonEncoder(JacksonJsonEncoder(sosialhjelpJsonMapper))
            }
            .baseUrl(clientProperties.fiksDigisosEndpointUrl)
            .build()
    }
}
