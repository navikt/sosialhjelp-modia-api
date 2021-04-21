package no.nav.sosialhjelp.modia.client.pdl

data class PdlRequest(
    val query: String,
    val variables: Variables
)

data class Variables(
    val ident: String,
    val navnHistorikk: Boolean = false
)
