package no.nav.sbl.sosialhjelpmodiaapi.client.msgraph

import no.nav.sbl.sosialhjelpmodiaapi.common.MsGraphException
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.subjecthandler.SubjectHandlerUtils
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.BEARER
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate


interface MsGraphClient {

    fun hentOnPremisesSamAccountName(): OnPremisesSamAccountName
}

@Component
@Profile("!mock")
class MsGraphClientImpl(
        private val restTemplate: RestTemplate
) : MsGraphClient {

    override fun hentOnPremisesSamAccountName(): OnPremisesSamAccountName {
        try {
            val url = "https://graph.microsoft.com/v1.0/me?\$select=$ON_PREMISES_SAM_ACCOUNT_NAME_FIELD"
            val headers = createRequestEntity()
            val response = restTemplate.exchange(url, HttpMethod.GET, headers, OnPremisesSamAccountName::class.java)
            return response.body!!
        } catch (e: RestClientException) {
            throw MsGraphException("MsGraph hentOnPremisesSamAccountName feilet", e)
        }
    }

    private fun createRequestEntity(): HttpEntity<Nothing> {
        val headers = IntegrationUtils.forwardHeaders()
        val token = SubjectHandlerUtils.getToken()
        headers.set(HttpHeaders.ACCEPT, APPLICATION_JSON_VALUE)
        headers.set(HttpHeaders.AUTHORIZATION, token)
        return HttpEntity(headers)
    }

    companion object {
        private const val ON_PREMISES_SAM_ACCOUNT_NAME_FIELD = "onPremisesSamAccountName"

        private val log by logger()
    }
}

data class OnPremisesSamAccountName(
        val onPremisesSamAccountName: String // NavIdent
)