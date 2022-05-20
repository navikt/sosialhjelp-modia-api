package no.nav.sosialhjelp.modia.tilgang.azure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.tilgang.azure.model.AzuredingsResponse
import org.springframework.context.annotation.Profile
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
    private val proxiedWebClientBuilder: WebClient.Builder,
    private val clientProperties: ClientProperties,
) : AzureAppTokenUtils {

    private val azureAppTokenWebClient: WebClient
        get() = proxiedWebClientBuilder.build()

    override fun hentTokenMedSkjermedePersonerScope(): String {
        return runBlocking(Dispatchers.IO) {
            val params = LinkedMultiValueMap<String, String>()
            params.add("client_id", clientProperties.azureClientId)
            params.add("scope", clientProperties.skjermedePersonerScope)
            params.add("client_secret", clientProperties.azureClientSecret)
            params.add("grant_type", "client_credentials")

            val tokenResponse: AzuredingsResponse = azureAppTokenWebClient.post()
                .uri(clientProperties.azureTokenEndpointUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .awaitBody()
            tokenResponse.accessToken
        }
    }
}

@Profile("(mock-alt | local | test)")
@Component
class MockAzureAppTokenUtils : AzureAppTokenUtils {
    override fun hentTokenMedSkjermedePersonerScope(): String {
        return "skjermede-personer-pip-token"
    }
}
