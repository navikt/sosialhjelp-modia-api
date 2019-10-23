package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonVilkar
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.Sak
import no.nav.sbl.sosialhjelpmodiaapi.domain.Utbetaling
import no.nav.sbl.sosialhjelpmodiaapi.domain.Vilkar

fun InternalDigisosSoker.apply(hendelse: JsonVilkar) {
    hendelse.beskrivelse
    hendelse.status
    hendelse.utbetalingsreferanse


    val utbetalinger: java.util.ArrayList<Utbetaling> = ArrayList()
    val vilkarSaker: java.util.ArrayList<Sak> = ArrayList()
    for (utbetalingsreferanse in hendelse.utbetalingsreferanse) {
        for (sak in saker) {
            for (utbetaling in sak.utbetalinger) {
                if (utbetaling.referanse == utbetalingsreferanse) {
                    utbetalinger.add(utbetaling)
                }
            }
        }
    }
    val vilkar = Vilkar("hendelse.referanse", utbetalinger, hendelse.beskrivelse, hendelse.status == JsonVilkar.Status.OPPFYLT)

    vilkarSaker.forEach { sak ->
        sak.vilkar.add(vilkar)
    }

    utbetalinger.forEach { utbetaling ->
        utbetaling.vilkar.add(vilkar)
    }

}