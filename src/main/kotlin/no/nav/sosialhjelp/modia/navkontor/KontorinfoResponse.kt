package no.nav.sosialhjelp.modia.navkontor

import no.nav.sosialhjelp.modia.navkontor.norg.BrukerKontakt

data class KontorinfoResponse(
    val enhetsnr: String,
    val navn: String,
    val kontorinfo: String,
    val brukerKontakt: BrukerKontakt,
    val norgUrl: String
)
