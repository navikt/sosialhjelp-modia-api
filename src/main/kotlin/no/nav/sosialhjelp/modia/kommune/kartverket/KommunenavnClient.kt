package no.nav.sosialhjelp.modia.kommune.kartverket

import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.typeRef
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Component
class KommunenavnClient(
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient,
) {
    private val kommunenavnWebClient: WebClient =
        webClientBuilder
            .clientConnector(ReactorClientHttpConnector(proxiedHttpClient))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }
            .build()

    fun getAll(): KommunenavnProperties {
        return kommunenavnWebClient.get()
            .uri("https://register.geonorge.no/sosi-kodelister/inndelinger/inndelingsbase/kommunenummer.json")
            .retrieve()
            .bodyToMono(typeRef<KommunenavnProperties>())
            .doOnError {
                log.warn("Kartverket - henting av info feilet:", it)
            }
            .block()!!
    }

    companion object {
        private val log by logger()
    }
}
