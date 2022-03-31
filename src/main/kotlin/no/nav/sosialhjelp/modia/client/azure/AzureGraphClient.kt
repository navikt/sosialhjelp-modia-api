package no.nav.sosialhjelp.modia.client.azure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.modia.client.azure.model.AzureAdGrupper
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class AzureGraphClient(
    private val proxiedWebClient: WebClient,
    private val azuredingsService: AzuredingsService,
    private val clientProperties: ClientProperties,
) {
    fun hentInnloggetVeilederSineGrupper(token: String): AzureAdGrupper {
        return runBlocking(Dispatchers.IO) {
            val excangedToken = azuredingsService.exchangeToken(token, "https://graph.microsoft.com/.default")
            proxiedWebClient.get()
                .uri("${clientProperties.azureGraphUrl}/me/memberOf")
                .header(HttpHeaders.AUTHORIZATION, BEARER + excangedToken)
                .retrieve()
                .awaitBody()
        }
    }
}
