package no.nav.sosialhjelp.modia.soknad.hendelser

data class HendelseResponse(
    val tittel: String,
    val tidspunkt: String,
    val beskrivelse: String?,
    val filbeskrivelse: String?,
)
