package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonUtbetaling
import no.nav.sbl.sosialhjelpmodiaapi.domain.*
import no.nav.sbl.sosialhjelpmodiaapi.saksstatus.DEFAULT_TITTEL
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

fun InternalDigisosSoker.apply(hendelse: JsonUtbetaling) {
    val utbetaling = Utbetaling(
            referanse = hendelse.utbetalingsreferanse,
            status = UtbetalingsStatus.valueOf(hendelse.status?.value() ?: JsonUtbetaling.Status.PLANLAGT_UTBETALING.value()),
            belop = BigDecimal.valueOf(hendelse.belop ?: 0.0),
            beskrivelse = hendelse.beskrivelse,
            posteringsDato = if (hendelse.forfallsdato == null) null else LocalDate.parse(hendelse.forfallsdato, ISO_LOCAL_DATE),
            utbetalingsDato = if (hendelse.utbetalingsdato == null) null else LocalDate.parse(hendelse.utbetalingsdato, ISO_LOCAL_DATE),
            fom = if (hendelse.fom == null) null else LocalDate.parse(hendelse.fom, ISO_LOCAL_DATE),
            tom = if (hendelse.tom == null) null else LocalDate.parse(hendelse.tom, ISO_LOCAL_DATE),
            mottaker = hendelse.mottaker,
            utbetalingsform = hendelse.utbetalingsmetode,
            vilkar = mutableListOf(),
            dokumentasjonkrav = mutableListOf()
    )

    var sakForReferanse = saker.firstOrNull { it.referanse == hendelse.saksreferanse } ?: saker.firstOrNull { it.referanse == "default" }

    if (sakForReferanse == null) {
        // Opprett ny Sak
        sakForReferanse = Sak(
                referanse = hendelse.saksreferanse ?: "default",
                saksStatus = SaksStatus.UNDER_BEHANDLING,
                tittel = DEFAULT_TITTEL,
                utbetalinger = mutableListOf(),
                vedtak = mutableListOf(),
                vilkar = mutableListOf(),
                dokumentasjonkrav = mutableListOf()
        )
        saker.add(sakForReferanse)
    }

    sakForReferanse.utbetalinger.add(utbetaling)

}