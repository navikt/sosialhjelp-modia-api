package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonUtbetaling
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.Utbetaling
import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingsStatus
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDate
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime
import java.math.BigDecimal

fun InternalDigisosSoker.apply(hendelse: JsonUtbetaling) {
    val utbetaling = Utbetaling(
            referanse = hendelse.utbetalingsreferanse,
            status = UtbetalingsStatus.valueOf(hendelse.status?.value()
                    ?: JsonUtbetaling.Status.PLANLAGT_UTBETALING.value()),
            belop = BigDecimal.valueOf(hendelse.belop ?: 0.0),
            beskrivelse = hendelse.beskrivelse,
            forfallsDato = if (hendelse.forfallsdato == null) null else hendelse.forfallsdato.toLocalDate(),
            utbetalingsDato = if (hendelse.utbetalingsdato == null) null else hendelse.utbetalingsdato.toLocalDate(),
            fom = if (hendelse.fom == null) null else hendelse.fom.toLocalDate(),
            tom = if (hendelse.tom == null) null else hendelse.tom.toLocalDate(),
            mottaker = hendelse.mottaker,
            annenMottaker = isAnnenMottaker(hendelse),
            kontonummer = if (isAnnenMottaker(hendelse)) null else hendelse.kontonummer,
            utbetalingsmetode = hendelse.utbetalingsmetode,
            vilkar = mutableListOf(),
            dokumentasjonkrav = mutableListOf(),
            datoHendelse = hendelse.hendelsestidspunkt.toLocalDateTime()
    )

    val sakForReferanse = saker.firstOrNull { it.referanse == hendelse.saksreferanse }
            ?: saker.firstOrNull { it.referanse == "default" }

    sakForReferanse?.utbetalinger?.removeIf { t -> t.referanse == utbetaling.referanse }
    sakForReferanse?.utbetalinger?.add(utbetaling)
    utbetalinger.removeIf { t -> t.referanse == utbetaling.referanse }
    utbetalinger.add(utbetaling)

}

private fun isAnnenMottaker(hendelse: JsonUtbetaling) =
        hendelse.annenMottaker == null || hendelse.annenMottaker