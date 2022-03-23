package no.nav.sosialhjelp.modia.client.azure

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.resolver.DefaultAddressResolverGroup
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

fun buildWebClient(webClientBuilder: WebClient.Builder, url: String, headers: HttpHeaders = applicationJsonHttpHeaders()): WebClient =
    webClientBuilder
        .baseUrl(url)
        .defaultHeaders { headers.map { it.key to it.value } }
        .clientConnector(
            ReactorClientHttpConnector(
                HttpClient.newConnection()
                    .resolver(DefaultAddressResolverGroup.INSTANCE)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000)
                    .doOnConnected { it.addHandlerLast(ReadTimeoutHandler(60)) }
            )
        )
        .build()

fun applicationJsonHttpHeaders(): HttpHeaders {
    val headers = HttpHeaders()
    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    return headers
}
