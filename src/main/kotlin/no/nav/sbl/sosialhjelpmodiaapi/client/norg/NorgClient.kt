package no.nav.sbl.sosialhjelpmodiaapi.client.norg

import no.nav.sbl.sosialhjelpmodiaapi.domain.NavEnhet

interface NorgClient {

    fun hentNavEnhet(enhetsnr: String): NavEnhet
}
