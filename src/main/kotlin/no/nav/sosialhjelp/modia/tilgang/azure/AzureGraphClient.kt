package no.nav.sosialhjelp.modia.tilgang.azure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.tilgang.azure.model.AzureAdGruppe
import no.nav.sosialhjelp.modia.tilgang.azure.model.AzureAdGrupper
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import org.springframework.context.annotation.Profile
import no.nav.sosialhjelp.modia.utils.configureWebClient
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient

interface AzureGraphClient {
    fun hentInnloggetVeilederSineGrupper(token: String): AzureAdGrupper
}

@Component
@Profile("!gcp")
class AzureGraphClientFss(
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient,
    private val azuredingsService: AzuredingsService,
    private val clientProperties: ClientProperties,
): AzureGraphClient {

    private val azureGraphWebClient: WebClient = webClientBuilder.configureWebClient(proxiedHttpClient)

    override fun hentInnloggetVeilederSineGrupper(token: String): AzureAdGrupper =
        runBlocking(Dispatchers.IO) {
            val excangedToken = azuredingsService.exchangeToken(token, "https://graph.microsoft.com/.default")
            azureGraphWebClient
                .get()
                .uri("${clientProperties.azureGraphUrl}/me/memberOf")
                .header(HttpHeaders.AUTHORIZATION, BEARER + excangedToken)
                .retrieve()
                .awaitBody()
        }
}

@Component
@Profile("gcp")
class AzureGraphClientGcp(
    private val tokenValidationContextHolder: TokenValidationContextHolder,
) : AzureGraphClient {
    override fun hentInnloggetVeilederSineGrupper(token: String): AzureAdGrupper {
        val claims = tokenValidationContextHolder.getTokenValidationContext().getClaims("azuread")
        if (claims.get("groups") == null) {
            error("groups ikke funnet i token claims")
        }
        return AzureAdGrupper(claims.getAsList("groups").map { AzureAdGruppe(it, null) })
    }
}
