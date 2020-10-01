package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonTildeltNavKontor
import no.nav.sbl.sosialhjelpmodiaapi.common.NorgException
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.NavKontorInformasjon
import no.nav.sbl.sosialhjelpmodiaapi.domain.SendingType
import no.nav.sbl.sosialhjelpmodiaapi.event.Titler.SOKNAD_VIDERESENDT
import no.nav.sbl.sosialhjelpmodiaapi.client.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime

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
        norgClient.hentNavEnhet(hendelse.navKontor)!!.navn
    } catch (e: NorgException) {
        "et annet NAV-kontor"
    }
    val beskrivelse = "Søknaden med vedlegg er videresendt og mottatt ved $destinasjon. Videresendingen vil ikke påvirke saksbehandlingstiden."
    historikk.add(Hendelse(SOKNAD_VIDERESENDT, beskrivelse, hendelse.hendelsestidspunkt.toLocalDateTime()))
    navKontorHistorikk.add(NavKontorInformasjon(SendingType.VIDERESENDT, hendelse.hendelsestidspunkt.toLocalDateTime(), hendelse.navKontor, destinasjon))
}