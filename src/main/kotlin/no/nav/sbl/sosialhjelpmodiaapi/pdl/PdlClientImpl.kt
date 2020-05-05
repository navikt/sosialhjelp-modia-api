package no.nav.sbl.sosialhjelpmodiaapi.pdl

import no.nav.sbl.sosialhjelpmodiaapi.common.PdlException
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
                throw PdlException(
                        firstError.extensions.code?.toUpperCase()?.let { HttpStatus.valueOf(it) },
                        "Message: ${firstError.message}, Classification: ${firstError.extensions.classification}"
                )
            }
            return pdlPersonResponse.data
        } catch (e: RestClientResponseException) {
            log.error("PDL - ${e.rawStatusCode} ${e.statusText} feil ved henting av navn, requesturl: $baseurl", e)
            throw PdlException(HttpStatus.valueOf(e.rawStatusCode), e.message)
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

    companion object {
        private val log by logger()
    }
}
