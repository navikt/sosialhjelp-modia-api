package no.nav.sosialhjelp.modia.kommune.kartverket

import no.nav.sosialhjelp.modia.logger
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class KommunenavnClient(
    restClientBuilder: RestClient.Builder,
) {
    private val kommunenavnRestClient = restClientBuilder.build()

    fun getAll(): KommunenavnProperties = try {
        kommunenavnRestClient
            .get()
            .uri("https://register.geonorge.no/api/sosi-kodelister/inndelinger/inndelingsbase/kommunenummer.json")
            .retrieve()
            .body(KommunenavnProperties::class.java)
            ?: throw RuntimeException("Kartverket - tom respons ved henting av kommunenavn")
    } catch (e: RestClientResponseException) {
        log.warn("Kartverket - henting av info feilet:", e)
        throw e
    }

    companion object {
        private val log by logger()
    }
}
