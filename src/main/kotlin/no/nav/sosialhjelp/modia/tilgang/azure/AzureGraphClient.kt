package no.nav.sosialhjelp.modia.tilgang.azure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.tilgang.azure.model.AzureAdGrupper
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient

@Component
class AzureGraphClient(
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient,
    private val azuredingsService: AzuredingsService,
    private val clientProperties: ClientProperties
) {

    private val azureGraphWebClient: WebClient =
        webClientBuilder
            .clientConnector(ReactorClientHttpConnector(proxiedHttpClient))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }
            .build()

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
