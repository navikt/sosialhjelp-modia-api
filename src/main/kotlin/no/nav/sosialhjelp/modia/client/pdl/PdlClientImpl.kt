package no.nav.sosialhjelp.modia.client.pdl

import no.nav.sosialhjelp.modia.client.sts.STSClient
import no.nav.sosialhjelp.modia.common.PdlException
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_CALL_ID
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_CONSUMER_TOKEN
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_TEMA
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.TEMA_KOM
import no.nav.sosialhjelp.modia.utils.mdc.MDCUtils.getCallId
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Profile("!(mock | local)")
@Component
class PdlClientImpl(
    clientProperties: ClientProperties,
    private val restTemplate: RestTemplate,
    private val stsClient: STSClient
) : PdlClient {

    private val baseurl = clientProperties.pdlEndpointUrl

    override fun hentPerson(ident: String): PdlHentPerson? {
        val query = getResourceAsString("/pdl/hentPerson.graphql").replace("[\n\r]", "")
        try {
            val requestEntity = createRequestEntity(PdlRequest(query, Variables(ident)))
            val response = restTemplate.exchange(baseurl, HttpMethod.POST, requestEntity, PdlPersonResponse::class.java)

            val pdlPersonResponse: PdlPersonResponse = response.body!!
            if (pdlPersonResponse.errors != null && pdlPersonResponse.errors.isNotEmpty()) {
                pdlPersonResponse.errors
                    .forEach { log.error("PDL - noe feilet. Message=${it.message}, path=${it.path}, code=${it.extensions.code}, classification=${it.extensions.classification}") }
                val firstError = pdlPersonResponse.errors[0]
                val statusCode = firstError.extensions.code?.toUpperCase()?.let { HttpStatus.valueOf(it) }
                throw PdlException(
                    "StatusCode: $statusCode, Message: ${firstError.message}, Classification: ${firstError.extensions.classification}"
                )
            }
            return pdlPersonResponse.data
        } catch (e: RestClientResponseException) {
            log.error("PDL - ${e.rawStatusCode} ${e.statusText} feil ved henting av navn, requesturl: $baseurl", e)
            throw PdlException(e.message)
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
        headers.set(HEADER_CALL_ID, getCallId())
        headers.set(HEADER_CONSUMER_TOKEN, BEARER + stsToken)
        headers.set(AUTHORIZATION, BEARER + stsToken)
        headers.set(HEADER_TEMA, TEMA_KOM)
        return HttpEntity(request, headers)
    }

    companion object {
        private val log by logger()
    }
}
