package no.nav.sosialhjelp.modia.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonVilkar
import no.nav.sosialhjelp.modia.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.domain.Utbetaling
import no.nav.sosialhjelp.modia.domain.Vilkar
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonVilkar) {

    val log by logger()

    val utbetalinger = finnAlleUtbetalingerSomVilkarRefererTil(hendelse)

    fjernFraUtbetalingerSomIkkeLegereErReferertTilIVilkaret(hendelse)

    if (utbetalinger.isEmpty()) {
        log.warn("Fant ingen utbetalinger å knytte vilkår til. Utbetalingsreferanser: ${hendelse.utbetalingsreferanse}")
        return
    }

    val vilkar = Vilkar(
            referanse = hendelse.vilkarreferanse,
            beskrivelse = hendelse.beskrivelse,
            oppfyllt = hendelse.status == JsonVilkar.Status.OPPFYLT,
            datoLagtTil = hendelse.hendelsestidspunkt.toLocalDateTime(),
            datoSistEndret = hendelse.hendelsestidspunkt.toLocalDateTime()
    )

    utbetalinger.forEach { it.vilkar.oppdaterEllerLeggTilVilkar(hendelse, vilkar) }
}

private fun InternalDigisosSoker.finnAlleUtbetalingerSomVilkarRefererTil(hendelse: JsonVilkar): MutableList<Utbetaling> {
    val utbetalinger = mutableListOf<Utbetaling>()
    for (utbetalingsreferanse in hendelse.utbetalingsreferanse) {
        for (sak in saker) {
            for (utbetaling in sak.utbetalinger) {
                if (utbetaling.referanse == utbetalingsreferanse) {
                    utbetalinger.add(utbetaling)
                }
            }
        }
    }
    return utbetalinger
}

private fun InternalDigisosSoker.fjernFraUtbetalingerSomIkkeLegereErReferertTilIVilkaret(hendelse: JsonVilkar) {
    for (sak in saker) {
        for (utbetaling in sak.utbetalinger) {
            utbetaling.vilkar.removeAll {
                it.referanse == hendelse.vilkarreferanse
                        && !hendelse.utbetalingsreferanse.contains(utbetaling.referanse)
            }
        }
    }
}

private fun MutableList<Vilkar>.oppdaterEllerLeggTilVilkar(hendelse: JsonVilkar, vilkar: Vilkar) {
    if (any { it.referanse == hendelse.vilkarreferanse }) {
        filter { it.referanse == hendelse.vilkarreferanse }
                .forEach { it.oppdaterFelter(hendelse) }
    } else {
        this.add(vilkar)
    }
}

private fun Vilkar.oppdaterFelter(hendelse: JsonVilkar) {
    datoSistEndret = hendelse.hendelsestidspunkt.toLocalDateTime()
    beskrivelse = hendelse.beskrivelse
    oppfyllt = hendelse.status == JsonVilkar.Status.OPPFYLT
}
