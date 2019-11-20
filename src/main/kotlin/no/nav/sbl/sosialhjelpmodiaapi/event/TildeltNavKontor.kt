package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonTildeltNavKontor
import no.nav.sbl.sosialhjelpmodiaapi.common.NorgException
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonTildeltNavKontor, norgClient: NorgClient) {

    if (hendelse.navKontor == tildeltNavKontor) {
        return
    }

    if (hendelse.navKontor == soknadsmottaker?.navEnhetsnummer) {
        return
    }

    tildeltNavKontor = hendelse.navKontor

    val detalj = try {
        val navKontorNavn = norgClient.hentNavEnhet(hendelse.navKontor).navn
        navKontorNavn
    } catch (e: NorgException) {
        "et annet NAV-kontor"
    }
    val beskrivelse = "Søknaden med vedlegg er videresendt og mottatt ved $detalj. Videresendingen vil ikke påvirke saksbehandlingstiden."
    historikk.add(Hendelse(beskrivelse, toLocalDateTime(hendelse.hendelsestidspunkt)))
}