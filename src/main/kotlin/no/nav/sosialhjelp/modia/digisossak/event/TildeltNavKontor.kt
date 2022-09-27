package no.nav.sosialhjelp.modia.digisossak.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonTildeltNavKontor
import no.nav.sosialhjelp.modia.app.exceptions.NorgException
import no.nav.sosialhjelp.modia.digisossak.domain.Hendelse
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.NavKontorInformasjon
import no.nav.sosialhjelp.modia.digisossak.domain.SendingType
import no.nav.sosialhjelp.modia.digisossak.domain.Soknadsmottaker
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
        norgClient.hentNavEnhet(hendelse.navKontor)?.navn?.takeUnless { it.isEmpty() }
            ?: "[Kan ikke hente NAV-kontor for \"${hendelse.navKontor}\"]"
    } catch (e: NorgException) {
        "et annet NAV-kontor"
    }
    soknadsmottaker = Soknadsmottaker(navEnhetsnummer = hendelse.navKontor, navEnhetsnavn = destinasjon)
    val beskrivelse = "Søknaden med vedlegg er videresendt og mottatt ved $destinasjon. Videresendingen vil ikke påvirke saksbehandlingstiden."
    historikk.add(
        Hendelse(
            tittel = SOKNAD_VIDERESENDT,
            beskrivelse = beskrivelse,
            tidspunkt = hendelse.hendelsestidspunkt.toLocalDateTime()
        )
    )
    navKontorHistorikk.add(
        NavKontorInformasjon(
            type = SendingType.VIDERESENDT,
            tidspunkt = hendelse.hendelsestidspunkt.toLocalDateTime(),
            navEnhetsnummer = hendelse.navKontor,
            navEnhetsnavn = destinasjon
        )
    )
}
