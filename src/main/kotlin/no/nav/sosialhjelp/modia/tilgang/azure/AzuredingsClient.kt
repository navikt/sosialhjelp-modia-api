package no.nav.sosialhjelp.modia.tilgang.azure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.sosialhjelp.modia.tilgang.azure.model.AzuredingsResponse
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient

@Component
class AzuredingsClient(
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient,
    private val azuredingsWebConfig: AzuredingsWebConfig
) {

    private val azuredingsWebClient: WebClient = webClientBuilder
        .clientConnector(ReactorClientHttpConnector(proxiedHttpClient))
        .codecs {
            it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
        }
        .build()

    suspend fun exchangeToken(subjectToken: String, clientAssertion: String, clientId: String, scope: String): AzuredingsResponse {
        return withContext(Dispatchers.IO) {
            val params = LinkedMultiValueMap<String, String>()
            params.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
            params.add("client_id", clientId)
            params.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
            params.add("client_assertion", clientAssertion)
            params.add("assertion", subjectToken)
            params.add("requested_token_use", "on_behalf_of")
            params.add("scope", scope)

            azuredingsWebClient
                .post()
                .uri(azuredingsWebConfig.tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .awaitBody()
        }
    }
}
