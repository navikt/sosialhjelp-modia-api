package no.nav.sosialhjelp.modia.app.maskinporten

data class MaskinportenResponse(
    val access_token: String,
    val token_type: String?,
    val expires_in: Int?,
    val scope: String?
)
