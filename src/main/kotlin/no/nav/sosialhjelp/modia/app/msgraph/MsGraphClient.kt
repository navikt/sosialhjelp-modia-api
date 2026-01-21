package no.nav.sosialhjelp.modia.app.msgraph

import no.nav.sosialhjelp.modia.app.exceptions.MsGraphException
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import no.nav.sosialhjelp.modia.utils.configureWebClient
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient

@Component
class MsGraphClient(
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient,
) {
    fun hentOnPremisesSamAccountName(accessToken: String): OnPremisesSamAccountName =
        msGraphWebClient
            .get()
            .uri("https://graph.microsoft.com/v1.0/me?\$select=$ON_PREMISES_SAM_ACCOUNT_NAME_FIELD")
            .header(HttpHeaders.ACCEPT, APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, BEARER + accessToken)
            .retrieve()
            .bodyToMono<OnPremisesSamAccountName>()
            .onErrorMap {
                MsGraphException("MsGraph hentOnPremisesSamAccountName feilet", it)
            }.block()!!

    private val msGraphWebClient: WebClient = webClientBuilder.configureWebClient(proxiedHttpClient)

    companion object {
        private const val ON_PREMISES_SAM_ACCOUNT_NAME_FIELD = "onPremisesSamAccountName"
    }
}

data class OnPremisesSamAccountName(
    val onPremisesSamAccountName: String, // NavIdent
)
