package no.nav.sosialhjelp.modia.utils

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sosialhjelp.modia.app.client.unproxiedHttpClient
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

val sosialhjelpJsonMapper: JsonMapper =
    JsonSosialhjelpObjectMapper
        .createJsonMapperBuilder()
        .addModule(kotlinModule())
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .build()

fun WebClient.Builder.configureBuilder(httpClient: HttpClient = unproxiedHttpClient()): WebClient.Builder =
    clientConnector( ReactorClientHttpConnector(httpClient))
        .codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }

fun WebClient.Builder.configureWebClient(httpClient: HttpClient = unproxiedHttpClient()): WebClient = configureBuilder(httpClient).build()

