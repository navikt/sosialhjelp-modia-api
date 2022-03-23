package no.nav.sosialhjelp.modia.client.azure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.modia.client.azure.model.AzuredingsResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

interface AzureAppTokenUtils {
    fun hentTokenMedSkjermedePersonerScope(): String
}

@Profile("!(mock-alt | local | test)")
@Component
class AzureAppTokenUtilsImpl(
    webClientBuilder: WebClient.Builder,
    @Value("\${dialog-api.azure_token_endpoint_url}") private val tokenEndpoint: String,
    @Value("\${dialog-api.azure_client_id}") private val clientId: String,
    @Value("\${dialog-api.azure_client_secret}") private val clientSecret: String,
    @Value("\${dialog-api.skjermede_personer_scope}") private val skjermedePersonerScope: String,
) : AzureAppTokenUtils {
    private val webClient: WebClient = buildWebClient(webClientBuilder, tokenEndpoint, applicationFormUrlencodedHeaders())

    override fun hentTokenMedSkjermedePersonerScope(): String {
        return runBlocking(Dispatchers.IO) {
            val params = LinkedMultiValueMap<String, String>()
            params.add("client_id", clientId)
            params.add("scope", skjermedePersonerScope)
            params.add("client_secret", clientSecret)
            params.add("grant_type", "client_credentials")

            val tokenResponse: AzuredingsResponse = webClient.post()
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .awaitBody()
            tokenResponse.accessToken
        }
    }

    final fun applicationFormUrlencodedHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        return headers
    }
}

@Profile("(mock-alt | local | test)")
@Component
class MockAzureAppTokenUtils : AzureAppTokenUtils {
    override fun hentTokenMedSkjermedePersonerScope(): String {
        return "skjermede-personer-pip-token"
    }
}
