package no.nav.sosialhjelp.modia.digisossak.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonDokumentasjonkrav
import no.nav.sosialhjelp.modia.digisossak.domain.Dokumentasjonkrav
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.OppgaveStatus
import no.nav.sosialhjelp.modia.digisossak.domain.Utbetaling
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonDokumentasjonkrav) {

    val log by logger()

    val dokumentasjonkrav = Dokumentasjonkrav(
        dokumentasjonkravId = hendelse.dokumentasjonkravreferanse,
        tittel = hendelse.tittel,
        beskrivelse = hendelse.beskrivelse,
        saksreferanse = hendelse.saksreferanse,
        status = OppgaveStatus.valueOf(hendelse.status.value()),
        utbetalingsReferanse = hendelse.utbetalingsreferanse,
        datoLagtTil = hendelse.hendelsestidspunkt.toLocalDateTime(),
        frist = hendelse.frist?.toLocalDateTime()
    )

    this.dokumentasjonkrav.oppdaterEllerLeggTilDokumentasjonkrav(hendelse, dokumentasjonkrav)

    val utbetalingerMedSakKnytning = mutableListOf<Utbetaling>()
    val utbetalingerUtenSakKnytning = mutableListOf<Utbetaling>()
    for (utbetalingsreferanse in hendelse.utbetalingsreferanse) {
        // utbetalinger knyttet til sak
        for (sak in saker) {
            for (utbetaling in sak.utbetalinger) {
                if (utbetaling.referanse == utbetalingsreferanse) {
                    utbetalingerMedSakKnytning.add(utbetaling)
                }
            }
        }
        // utbetalinger ikke knyttet til sak
        for (utbetalingUtenSak in utbetalinger) {
            if (utbetalingUtenSak.referanse == utbetalingsreferanse) {
                utbetalingerUtenSakKnytning.add(utbetalingUtenSak)
            }
        }
    }

    if (utbetalingerMedSakKnytning.isEmpty() && utbetalingerUtenSakKnytning.isEmpty()) {
        log.warn("Fant ingen utbetalinger å knytte dokumentasjonkrav til. Utbetalingsreferanser: ${hendelse.utbetalingsreferanse}")
        return
    }

    val union = utbetalingerMedSakKnytning.union(utbetalingerUtenSakKnytning)
    union.forEach { it.dokumentasjonkrav.oppdaterEllerLeggTilDokumentasjonkrav(hendelse, dokumentasjonkrav) }

//    if (featureToggles.dokumentasjonkravEnabled) {
//        val beskrivelse = "Dokumentasjonskravene dine er oppdatert, les mer i vedtaket."
//        historikk.add(Hendelse(beskrivelse, hendelse.hendelsestidspunkt.toLocalDateTime()))
//    }
}

private fun MutableList<Dokumentasjonkrav>.oppdaterEllerLeggTilDokumentasjonkrav(hendelse: JsonDokumentasjonkrav, dokumentasjonkrav: Dokumentasjonkrav) {
    if (any { it.dokumentasjonkravId == hendelse.dokumentasjonkravreferanse }) {
        filter { it.dokumentasjonkravId == hendelse.dokumentasjonkravreferanse }
            .forEach { it.oppdaterFelter(hendelse) }
    } else {
        this.add(dokumentasjonkrav)
    }
}

private fun Dokumentasjonkrav.oppdaterFelter(hendelse: JsonDokumentasjonkrav) {
    beskrivelse = hendelse.beskrivelse
    status = OppgaveStatus.valueOf(hendelse.status.value())
}
