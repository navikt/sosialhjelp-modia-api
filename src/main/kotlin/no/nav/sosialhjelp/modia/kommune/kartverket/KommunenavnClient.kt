package no.nav.sosialhjelp.modia.kommune.kartverket

import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.typeRef
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class KommunenavnClient(
    private val proxiedWebClient: WebClient,
) {
    fun getAll(): KommunenavnProperties {
        return proxiedWebClient.get()
            .uri("https://register.geonorge.no/api/subregister/sosi-kodelister/kartverket/kommunenummer-alle.json")
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
