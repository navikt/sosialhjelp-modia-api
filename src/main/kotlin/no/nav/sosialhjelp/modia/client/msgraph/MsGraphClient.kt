package no.nav.sosialhjelp.modia.client.msgraph

import no.nav.sosialhjelp.modia.common.MsGraphException
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.forwardHeaders
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate


@Component
class MsGraphClient(
        private val restTemplate: RestTemplate
) {

    fun hentOnPremisesSamAccountName(accessToken: String): OnPremisesSamAccountName {
        try {
            val url = "https://graph.microsoft.com/v1.0/me?\$select=$ON_PREMISES_SAM_ACCOUNT_NAME_FIELD"
            val headers = createRequestEntity(accessToken)
            val response = restTemplate.exchange(url, HttpMethod.GET, headers, OnPremisesSamAccountName::class.java)
            return response.body!!
        } catch (e: RestClientException) {
            throw MsGraphException("MsGraph hentOnPremisesSamAccountName feilet", e)
        }
    }

    private fun createRequestEntity(accessToken: String): HttpEntity<Nothing> {
        val headers = forwardHeaders()
        headers.set(HttpHeaders.ACCEPT, APPLICATION_JSON_VALUE)
        headers.set(HttpHeaders.AUTHORIZATION, BEARER + accessToken)
        return HttpEntity(headers)
    }

    companion object {
        private const val ON_PREMISES_SAM_ACCOUNT_NAME_FIELD = "onPremisesSamAccountName"
    }
}

data class OnPremisesSamAccountName(
        val onPremisesSamAccountName: String // NavIdent
)