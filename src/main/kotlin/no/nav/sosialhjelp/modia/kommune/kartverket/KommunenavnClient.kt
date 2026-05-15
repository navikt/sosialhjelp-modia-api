package no.nav.sosialhjelp.modia.kommune.kartverket

import no.nav.sosialhjelp.modia.logger
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body

@Component
class KommunenavnClient {
    private val kommunenavnRestClient = RestClient.builder().build()

    fun getAll(): KommunenavnProperties =
        try {
            kommunenavnRestClient
                .get()
                .uri("https://register.geonorge.no/api/sosi-kodelister/inndelinger/inndelingsbase/kommunenummer.json")
                .retrieve()
                .body<KommunenavnProperties>()
                ?: throw RuntimeException("Kartverket - tom respons ved henting av kommunenavn")
        } catch (e: RestClientResponseException) {
            log.warn("Kartverket - henting av info feilet:", e)
            throw e
        }

    companion object {
        private val log by logger()
    }
}
