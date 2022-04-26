package no.nav.sosialhjelp.modia.event

const val SOKNAD_DEFAULT_TITTEL = "Søknad om økonomisk sosialhjelp"

const val SAK_DEFAULT_TITTEL: String = "Økonomisk sosialhjelp"

const val VIS_SOKNADEN = "Søknaden til bruker følger med denne hendelsen"
const val VIS_BREVET = "Et brev fra veileder følger med denne hendelsen"

const val DEFAULT_NAVENHETSNAVN = "[Kan ikke hente NAV-kontor]"

fun navenhetsnavnOrDefault(navenhetsnavn: String?): String {
    return if (navenhetsnavn.isNullOrEmpty()) DEFAULT_NAVENHETSNAVN else navenhetsnavn
}
