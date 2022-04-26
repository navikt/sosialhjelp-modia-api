package no.nav.sosialhjelp.modia.client.msgraph

import no.nav.sosialhjelp.modia.app.exceptions.MsGraphException
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class MsGraphClient(
    private val proxiedWebClient: WebClient
) {

    fun hentOnPremisesSamAccountName(accessToken: String): OnPremisesSamAccountName {
        return proxiedWebClient.get()
            .uri("https://graph.microsoft.com/v1.0/me?\$select=$ON_PREMISES_SAM_ACCOUNT_NAME_FIELD")
            .header(HttpHeaders.ACCEPT, APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, BEARER + accessToken)
            .retrieve()
            .bodyToMono<OnPremisesSamAccountName>()
            .onErrorMap {
                MsGraphException("MsGraph hentOnPremisesSamAccountName feilet", it)
            }
            .block()!!
    }

    companion object {
        private const val ON_PREMISES_SAM_ACCOUNT_NAME_FIELD = "onPremisesSamAccountName"
    }
}

data class OnPremisesSamAccountName(
    val onPremisesSamAccountName: String // NavIdent
)