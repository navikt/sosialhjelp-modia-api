package no.nav.sosialhjelp.modia.digisossak.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonUtbetaling
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.Utbetaling
import no.nav.sosialhjelp.modia.digisossak.domain.UtbetalingsStatus
import no.nav.sosialhjelp.modia.toLocalDate
import no.nav.sosialhjelp.modia.toLocalDateTime
import java.math.BigDecimal

fun InternalDigisosSoker.apply(hendelse: JsonUtbetaling) {
    val utbetaling =
        Utbetaling(
            referanse = hendelse.utbetalingsreferanse,
            status =
                UtbetalingsStatus.valueOf(
                    hendelse.status?.name
                        ?: JsonUtbetaling.Status.PLANLAGT_UTBETALING.name,
                ),
            belop = BigDecimal.valueOf(hendelse.belop ?: 0.0),
            beskrivelse = hendelse.beskrivelse,
            forfallsDato = hendelse.forfallsdato?.toLocalDate(),
            utbetalingsDato = hendelse.utbetalingsdato?.toLocalDate(),
            fom = hendelse.fom?.toLocalDate(),
            tom = hendelse.tom?.toLocalDate(),
            mottaker = hendelse.mottaker,
            annenMottaker = isAnnenMottaker(hendelse),
            kontonummer = if (isAnnenMottaker(hendelse)) null else hendelse.kontonummer,
            utbetalingsmetode = hendelse.utbetalingsmetode,
            vilkar = mutableListOf(),
            dokumentasjonkrav = mutableListOf(),
            datoHendelse = hendelse.hendelsestidspunkt.toLocalDateTime(),
        )

    val sakForReferanse =
        saker.firstOrNull { it.referanse == hendelse.saksreferanse }
            ?: saker.firstOrNull { it.referanse == "default" }

    sakForReferanse?.utbetalinger?.removeIf { t -> t.referanse == utbetaling.referanse }
    sakForReferanse?.utbetalinger?.add(utbetaling)
    utbetalinger.removeIf { t -> t.referanse == utbetaling.referanse }
    utbetalinger.add(utbetaling)
}

private fun isAnnenMottaker(hendelse: JsonUtbetaling) = hendelse.annenMottaker == true
