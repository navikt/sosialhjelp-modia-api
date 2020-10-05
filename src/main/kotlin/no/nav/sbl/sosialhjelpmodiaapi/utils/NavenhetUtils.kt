package no.nav.sbl.sosialhjelpmodiaapi.utils

const val DEFAULT_NAVENHETSNAVN = "[Kan ikke hente NAV-kontor]"

fun navenhetsnavnOrDefault(navenhetsnavn: String?): String {
    return if (navenhetsnavn.isNullOrEmpty()) DEFAULT_NAVENHETSNAVN else navenhetsnavn
}
