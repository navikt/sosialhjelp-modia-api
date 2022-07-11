package no.nav.sosialhjelp.modia.digisossak.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonTildeltNavKontor
import no.nav.sosialhjelp.modia.app.exceptions.NorgException
import no.nav.sosialhjelp.modia.digisossak.domain.Hendelse
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.NavKontorInformasjon
import no.nav.sosialhjelp.modia.digisossak.domain.SendingType
import no.nav.sosialhjelp.modia.digisossak.event.Titler.SOKNAD_VIDERESENDT
import no.nav.sosialhjelp.modia.navkontor.norg.NorgClient
import no.nav.sosialhjelp.modia.toLocalDateTime

fun InternalDigisosSoker.apply(
    hendelse: JsonTildeltNavKontor,
    norgClient: NorgClient
) {

    val behandlendeNavKontor = navKontorHistorikk.lastOrNull()
    if (hendelse.navKontor == behandlendeNavKontor?.navEnhetsnummer) {
        return
    }

    if (hendelse.navKontor == soknadsmottaker?.navEnhetsnummer) {
//        tildeltNavKontor = hendelse.navKontor
        return
    }

    val destinasjon = try {
        val navn = norgClient.hentNavEnhet(hendelse.navKontor)?.navn
        if (navn.isNullOrEmpty()) "[Kan ikke hente NAV-kontor for \"${hendelse.navKontor}\"]" else navn
    } catch (e: NorgException) {
        "et annet NAV-kontor"
    }
    val beskrivelse = "Søknaden med vedlegg er videresendt og mottatt ved $destinasjon, {{kommunenavn}}. Videresendingen vil ikke påvirke saksbehandlingstiden."
    historikk.add(Hendelse(SOKNAD_VIDERESENDT, beskrivelse, hendelse.hendelsestidspunkt.toLocalDateTime()))
    navKontorHistorikk.add(NavKontorInformasjon(SendingType.VIDERESENDT, hendelse.hendelsestidspunkt.toLocalDateTime(), hendelse.navKontor, destinasjon))
}
