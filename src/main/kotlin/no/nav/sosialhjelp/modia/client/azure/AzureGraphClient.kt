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
    webClientBuilder: WebClient.Builder,
    clientProperties: ClientProperties,
) {
    val webClient = buildWebClient(webClientBuilder, clientProperties.azureGraphUrl)

    fun hentInnloggetVeilederSineGrupper(token: String): AzureAdGrupper {
        return runBlocking(Dispatchers.IO) {
            webClient.get()
                .uri("/me/memberOf")
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .retrieve()
                .awaitBody()
        }
    }
}
