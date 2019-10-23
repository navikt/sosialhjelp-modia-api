package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonTildeltNavKontor
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonTildeltNavKontor, norgClient: NorgClient) {

    if (hendelse.navKontor == soknadsmottaker?.navEnhetsnummer) {
        return
    }

    val navKontorNavn = norgClient.hentNavEnhet(hendelse.navKontor).navn
    val beskrivelse = "Søknaden med vedlegg er videresendt og mottatt ved $navKontorNavn. Videresendingen vil ikke påvirke saksbehandlingstiden"

    historikk.add(Hendelse(beskrivelse, toLocalDateTime(hendelse.hendelsestidspunkt)))
}