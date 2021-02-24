package no.nav.sosialhjelp.modia.client.kommunenavn

import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.typeRef
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.forwardHeaders
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

class KommunenavnClient(
        private val restTemplate: RestTemplate
) {
    fun getAll(): KommunenavnProperties {
        try {
            val response = restTemplate.exchange(
                    "https://register.geonorge.no/api/subregister/sosi-kodelister/kartverket/kommunenummer-alle.json",
                    HttpMethod.GET,
                    HttpEntity<Nothing>(forwardHeaders()),
                    typeRef<KommunenavnProperties>()
            )
            return response.body!!
        } catch (e: HttpClientErrorException) {
            log.warn("Kartverket - henting av info feilet:", e)
            throw(e)
        } catch (e: Exception) {
            log.warn("Kartverket - henting av info feilet:", e)
            throw(e)
        }
    }

    companion object {
        private val log by logger()
    }
}
