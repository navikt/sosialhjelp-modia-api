package no.nav.sosialhjelp.modia.soknad.dokumentasjonkrav

import no.nav.sosialhjelp.modia.digisossak.domain.Sak

const val DOKUMENTASJONKRAV_UTEN_SAK_TITTEL = "―"

internal fun hentSakstittel(saksreferanse: String?, saker: MutableList<Sak>): String {
    if (saksreferanse == null) {
        return DOKUMENTASJONKRAV_UTEN_SAK_TITTEL
    }
    return saker.firstOrNull { sak -> sak.referanse == saksreferanse }?.tittel ?: DOKUMENTASJONKRAV_UTEN_SAK_TITTEL
}
