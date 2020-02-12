package no.nav.sbl.sosialhjelpmodiaapi.pdl

import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.sts.STSClient
import no.nav.sbl.sosialhjelpmodiaapi.typeRef
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.BEARER
import no.nav.sbl.sosialhjelpmodiaapi.utils.generateCallId
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Component
class PdlClient(clientProperties: ClientProperties,
                private val restTemplate: RestTemplate,
                private val stsClient: STSClient) {

    companion object {
        val log by logger()
    }

    private val baseurl = clientProperties.pdlEndpointUrl

    fun hentPerson(): PdlHentPerson? {
        val fnr: String = "fnr" // fixme: fnr som input - etter at vi har sjekket at veileder har tilgang til å hente personinfo for bruker?

        val query = getResourceAsString("/pdl/hentPerson.graphql").replace("[\n\r]", "")

        val requestEntity = createRequestEntity(PdlRequest(query, Variables(fnr)))
        try {
            val response = restTemplate.exchange(baseurl, HttpMethod.POST, requestEntity, typeRef<PdlPersonResponse>())
            return response.body!!.data
        } catch (e: RestClientException) {
            log.error("PDL - feil ved henting av navn, requesturl: $baseurl", e)
            throw e
        }
    }

    fun ping() {
        try {
            restTemplate.exchange(baseurl, HttpMethod.OPTIONS, HttpEntity(null, null), String::class.java)
        } catch (e: RestClientException) {
            log.error("PDL - ping feilet, requesturl: $baseurl", e)
            throw e
        }
    }

    private fun getResourceAsString(path: String) = this::class.java.getResource(path).readText()

    private fun createRequestEntity(request: PdlRequest): HttpEntity<PdlRequest> {
        val stsToken: String = stsClient.token()
        val headers = HttpHeaders()
        headers.contentType = APPLICATION_JSON
        headers.set("Nav-Call-Id", generateCallId())
        headers.set("Nav-Consumer-Token", BEARER + stsToken)
        headers.set(AUTHORIZATION, BEARER + stsToken)
        headers.set("Tema", "TEMA_SOSIALHJELP") // tema for økonomisk sosialhjelp?
        return HttpEntity(request, headers)
    }
}