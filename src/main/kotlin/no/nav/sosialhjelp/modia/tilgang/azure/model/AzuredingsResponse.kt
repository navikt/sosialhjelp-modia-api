package no.nav.sosialhjelp.modia.tilgang.azure.model

import com.fasterxml.jackson.annotation.JsonProperty

data class AzuredingsResponse(
    @param:JsonProperty("token_type") val tokenType: String,
    @param:JsonProperty("scope") val scope: String = "",
    @param:JsonProperty("expires_in") val expiresIn: Int = 0,
    @param:JsonProperty("ext_expires_in") val extExpiresIn: Int = 0,
    @param:JsonProperty("access_token") val accessToken: String = "",
    @param:JsonProperty("refresh_token") val refreshToken: String = "",
)
