package no.nav.sosialhjelp.modia.client.azure.model

import com.fasterxml.jackson.annotation.JsonProperty

internal data class AzuredingsResponse(
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("scope") val scope: String = "",
    @JsonProperty("expires_in") val expiresIn: Int = 0,
    @JsonProperty("ext_expires_in") val extExpiresIn: Int = 0,
    @JsonProperty("access_token") val accessToken: String = "",
    @JsonProperty("refresh_token") val refreshToken: String = "",
)
