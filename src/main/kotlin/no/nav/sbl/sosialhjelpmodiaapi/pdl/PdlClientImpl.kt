package no.nav.sbl.sosialhjelpmodiaapi.pdl

import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.sts.STSClient
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.BEARER
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.NAV_CALL_ID
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.NAV_CONSUMER_TOKEN
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.TEMA
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.TEMA_KOM
import no.nav.sbl.sosialhjelpmodiaapi.utils.generateCallId
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Profile("!(mock | local)")
@Component
class PdlClientImpl(clientProperties: ClientProperties,
                    private val restTemplate: RestTemplate,
                    private val stsClient: STSClient) : PdlClient {

    companion object {
        val log by logger()
    }

    private val baseurl = clientProperties.pdlEndpointUrl

    override fun hentPerson(ident: String): PdlHentPerson? {
        // fixme: sjekk at veileder har tilgang til å hente personinfo for bruker med ident?
        val query = getResourceAsString("/pdl/hentPerson.graphql").replace("[\n\r]", "")
        try {
            log.info("query: $query")
            val requestEntity = createRequestEntity(PdlRequest(query, Variables(ident)))
            val response = restTemplate.exchange(baseurl, HttpMethod.POST, requestEntity, PdlPersonResponse::class.java)
            log.info("response: ${response.statusCode}")
            return response.body!!.data
        } catch (e: RestClientException) {
            log.error("PDL - feil ved henting av navn, requesturl: $baseurl", e)
            throw e
        }
    }

    override fun ping() {
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
        headers.set(NAV_CALL_ID, generateCallId())
        headers.set(NAV_CONSUMER_TOKEN, BEARER + stsToken)
        headers.set(AUTHORIZATION, BEARER + stsToken)
        headers.set(TEMA, TEMA_KOM)
        return HttpEntity(request, headers)
    }
}