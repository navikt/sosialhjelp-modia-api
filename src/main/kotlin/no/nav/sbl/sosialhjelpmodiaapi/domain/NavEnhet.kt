package no.nav.sbl.sosialhjelpmodiaapi.domain

data class NavEnhet(
    val enhetId: Int,
    val navn: String,
    val enhetNr: String,
    val status: String,
    val antallRessurser: Int,
    val aktiveringsdato: String,
    val nedleggelsesdato: String?,
    val sosialeTjenester: String?,
    val type: String?
)
