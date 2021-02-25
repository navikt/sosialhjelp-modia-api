package no.nav.sosialhjelp.modia.client.norg

import no.nav.sosialhjelp.modia.domain.NavEnhet

interface NorgClient {

    fun hentNavEnhet(enhetsnr: String): NavEnhet?

    fun hentAlleNavEnheter(): List<NavEnhet>
}
