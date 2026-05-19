package no.nav.sosialhjelp.modia.auth.texas

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.utils.sosialhjelpJsonMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

enum class TokenEndpointType {
    // For kall uten sluttbrukers kontekst
    M2M,

    // For kall med sluttbrukers kontekst
    BEHALF_OF,

    // For å sjekke et token
    INTROSPECTION,
}

sealed class TexasClient(
    private val tokenEndpoint: String,
    private val tokenXEndpoint: String,
) {
    protected val log by logger()

    private val texasRestClient =
        RestClient
            .builder()
            .defaultHeaders { it.contentType = MediaType.APPLICATION_JSON }
            .build()

    open fun getMaskinportenToken(): String = getToken(TokenEndpointType.M2M, maskinportenParams)

    fun introspectToken(
        token: String,
        identityProvider: IdentityProvider,
    ): IntrospectionResponse {
        val response: IntrospectionResponse =
            texasRestClient
                .post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf("token" to token, "identity_provider" to identityProvider.value))
                .retrieve()
                .body<IntrospectionResponse>()
                ?: error("Feil ved introspeksjon av token: tom respons")

        if (response.error != null) {
            log.debug("Feil ved introspeksjon av token: ${response.error}")
            error("Feil ved introspeksjon av token: ${response.error}")
        }
        if (!response.active) {
            log.debug("Token er inaktiv ved introspeksjon")
            error("hekkan")
        }
        return response
    }

    open fun getTokenXToken(
        target: String,
        userToken: String,
        identityProvider: IdentityProvider,
    ): String =
        getToken(
            TokenEndpointType.BEHALF_OF,
            getTokenXParams(target, userToken, identityProvider),
        )

    private val maskinportenParams: Map<String, String> = mapOf("identity_provider" to "maskinporten", "target" to "ks:fiks")

    private fun getTokenXParams(
        target: String,
        userToken: String,
        identityProvider: IdentityProvider,
    ): Map<String, String> = mapOf("identity_provider" to identityProvider.value, "target" to target, "user_token" to userToken)

    protected fun getToken(
        tokenEndpointType: TokenEndpointType,
        params: Map<String, String>,
    ): String {
        val url =
            when (tokenEndpointType) {
                TokenEndpointType.M2M -> tokenEndpoint
                TokenEndpointType.BEHALF_OF -> tokenXEndpoint
                TokenEndpointType.INTROSPECTION -> error("Cannot get token for introspection. Use introspectToken instead.")
            }

        return texasRestClient
            .post()
            .uri(url)
            .body(params)
            .retrieve()
            .onStatus({ it.isError }) { _, response ->
                val error =
                    runCatching {
                        sosialhjelpJsonMapper.readValue(response.body, TokenErrorResponse::class.java)
                    }.getOrNull()
                        ?: TokenErrorResponse(
                            "unknown_error",
                            "Status: ${response.statusCode}",
                        )
                error(
                    "Feil ved henting av $tokenEndpointType-token fra Texas. Statuscode: ${response.statusCode}. Error: $error",
                )
            }.body<TokenResponse>()
            ?.also {
                log.debug("Hentet {}-token fra Texas", tokenEndpointType)
            }?.accessToken
            ?: error("Tom respons fra Texas ved henting av $tokenEndpointType-token")
    }
}

@Component
@Profile("!(mock-alt|testcontainers)")
class TexasClientImpl(
    @param:Value("\${NAIS_TOKEN_ENDPOINT}")
    private val tokenEndpoint: String,
    @param:Value("\${NAIS_TOKEN_EXCHANGE_ENDPOINT}")
    private val tokenXEndpoint: String,
) : TexasClient(tokenEndpoint, tokenXEndpoint)

@Component
@Profile("mock-alt", "testcontainers")
class MockTexasClient(
    @param:Value("\${NAIS_TOKEN_ENDPOINT:http://localhost:8081/api/v1/token}")
    private val tokenEndpoint: String,
    @param:Value("\${NAIS_TOKEN_EXCHANGE_ENDPOINT:http://localhost:8081/api/v1/token/exchange}")
    private val tokenXEndpoint: String,
) : TexasClient(tokenEndpoint, tokenXEndpoint) {
    override fun getTokenXToken(
        target: String,
        userToken: String,
        identityProvider: IdentityProvider,
    ): String = "token-x-token"

    override fun getMaskinportenToken(): String = "token"
}

data class TokenResponse(
    @param:JsonProperty("access_token")
    val accessToken: String,
    @param:JsonProperty("expires_in")
    val expiresInSeconds: Int,
    @param:JsonProperty("token_type")
    val tokenType: String,
)

data class TokenErrorResponse(
    val error: String,
    @param:JsonProperty("error_description")
    val errorDescription: String,
)

enum class IdentityProvider(
    val value: String,
) {
    ENTRA_ID("entra_id"),
    TOKEN_X("token_x"),
}

data class IntrospectionResponse(
    val active: Boolean,
    @param:JsonInclude(JsonInclude.Include.NON_NULL)
    val error: String?,
    @param:JsonAnySetter @get:JsonAnyGetter
    val other: Map<String, Any?> = mutableMapOf(),
)
