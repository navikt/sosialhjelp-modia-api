package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonVilkar
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.Sak
import no.nav.sbl.sosialhjelpmodiaapi.domain.Vilkar
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonVilkar) {

    val tidligereVilkar = vilkar.filter { it.referanse == hendelse.vilkarreferanse }.firstOrNull()
    if (tidligereVilkar != null) {
        fjernVilkarFraAlleUtbetalinger(tidligereVilkar, saker)
        tidligereVilkar.beskrivelse = hendelse.beskrivelse
        tidligereVilkar.oppfyllt = hendelse.status == JsonVilkar.Status.OPPFYLT
        tidligereVilkar.datoSistEndret = hendelse.hendelsestidspunkt.toLocalDateTime()
        oppdaterUtbetalingerMedVilkar(hendelse, tidligereVilkar, saker)
    } else {
        val vilkar = Vilkar(
                referanse = hendelse.vilkarreferanse,
                beskrivelse = hendelse.beskrivelse,
                oppfyllt = hendelse.status == JsonVilkar.Status.OPPFYLT,
                datoLagtTil = hendelse.hendelsestidspunkt.toLocalDateTime(),
                datoSistEndret = hendelse.hendelsestidspunkt.toLocalDateTime()
        )
        this.vilkar.add(vilkar)
        oppdaterUtbetalingerMedVilkar(hendelse, vilkar, saker)
    }

//    historikk.add(Hendelse(
//            Titler.VILLKAR,
//            "Vilkår har blitt oppdatert",
//            hendelse.hendelsestidspunkt.toLocalDateTime(),
//            null)
//    )
}

fun oppdaterUtbetalingerMedVilkar(hendelse: JsonVilkar, vilkar: Vilkar, saker: List<Sak>) {
    for (utbetalingsreferanse in hendelse.utbetalingsreferanse) {
        for (sak in saker) {
            for (utbetaling in sak.utbetalinger) {
                if (utbetaling.referanse == utbetalingsreferanse) {
                    utbetaling.vilkar.add(vilkar)
                }
            }
        }
    }
}

fun fjernVilkarFraAlleUtbetalinger(vilkar: Vilkar, saker: List<Sak>) {
    for (sak in saker) {
        for (utbetaling in sak.utbetalinger) {
            utbetaling.vilkar.removeIf { it.referanse == vilkar.referanse }
        }
    }
}
