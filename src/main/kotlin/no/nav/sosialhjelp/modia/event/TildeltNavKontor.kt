package no.nav.sosialhjelp.modia.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonTildeltNavKontor
import no.nav.sosialhjelp.modia.common.NorgException
import no.nav.sosialhjelp.modia.domain.Hendelse
import no.nav.sosialhjelp.modia.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.domain.NavKontorInformasjon
import no.nav.sosialhjelp.modia.domain.SendingType
import no.nav.sosialhjelp.modia.event.Titler.SOKNAD_VIDERESENDT
import no.nav.sosialhjelp.modia.navkontor.norg.NorgClient
import no.nav.sosialhjelp.modia.toLocalDateTime
import no.nav.sosialhjelp.modia.utils.navenhetsnavnOrDefault

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
        navenhetsnavnOrDefault(navn)
    } catch (e: NorgException) {
        "et annet NAV-kontor"
    }
    val beskrivelse = "Søknaden med vedlegg er videresendt og mottatt ved $destinasjon, {{kommunenavn}}. Videresendingen vil ikke påvirke saksbehandlingstiden."
    historikk.add(Hendelse(SOKNAD_VIDERESENDT, beskrivelse, hendelse.hendelsestidspunkt.toLocalDateTime()))
    navKontorHistorikk.add(NavKontorInformasjon(SendingType.VIDERESENDT, hendelse.hendelsestidspunkt.toLocalDateTime(), hendelse.navKontor, destinasjon))
}
