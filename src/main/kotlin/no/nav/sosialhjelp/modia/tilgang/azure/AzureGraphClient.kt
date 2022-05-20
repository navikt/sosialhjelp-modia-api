package no.nav.sosialhjelp.modia.tilgang.azure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.tilgang.azure.model.AzureAdGrupper
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class AzureGraphClient(
    private val proxiedWebClientBuilder: WebClient.Builder,
    private val azuredingsService: AzuredingsService,
    private val clientProperties: ClientProperties,
) {

    private val azureGraphWebClient: WebClient
        get() = proxiedWebClientBuilder.build()

    fun hentInnloggetVeilederSineGrupper(token: String): AzureAdGrupper {
        return runBlocking(Dispatchers.IO) {
            val excangedToken = azuredingsService.exchangeToken(token, "https://graph.microsoft.com/.default")
            azureGraphWebClient.get()
                .uri("${clientProperties.azureGraphUrl}/me/memberOf")
                .header(HttpHeaders.AUTHORIZATION, BEARER + excangedToken)
                .retrieve()
                .awaitBody()
        }
    }
}
