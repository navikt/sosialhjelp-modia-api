package no.nav.sosialhjelp.modia.app.client

import io.netty.resolver.DefaultAddressResolverGroup
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.ProxyProvider
import java.net.URL

fun proxiedHttpClient(proxyUrl: String): HttpClient {
    val uri = URL(proxyUrl)

    return HttpClient.create()
        .resolver(DefaultAddressResolverGroup.INSTANCE)
        .proxy { proxy ->
            proxy.type(ProxyProvider.Proxy.HTTP).host(uri.host).port(uri.port)
        }
}

fun unproxiedHttpClient(): HttpClient = HttpClient
    .newConnection()
    .resolver(DefaultAddressResolverGroup.INSTANCE)
