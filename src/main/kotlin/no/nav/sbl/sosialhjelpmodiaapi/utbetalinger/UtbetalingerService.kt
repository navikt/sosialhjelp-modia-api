package no.nav.sbl.sosialhjelpmodiaapi.utbetalinger

import no.nav.sbl.sosialhjelpmodiaapi.domain.ManedUtbetaling
import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingerResponse
import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingsStatus
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.springframework.stereotype.Component
import java.text.DateFormatSymbols
import java.time.YearMonth


@Component
class UtbetalingerService(private val fiksClient: FiksClient,
                          private val eventService: EventService) {

    fun hentUtbetalinger(token: String): List<UtbetalingerResponse> {
        val digisosSaker = fiksClient.hentAlleDigisosSaker(token)

        if (digisosSaker.isEmpty()) {
            log.info("Fant ingen søknader for bruker")
            return emptyList()
        }

        val alleUtbetalinger: List<ManedUtbetaling> = digisosSaker
                .flatMap { digisosSak ->
                    val model = eventService.createModel(digisosSak, token)
                    model.saker
                            .flatMap { sak ->
                                sak.utbetalinger
                                        .filter { it.utbetalingsDato != null && (it.status == UtbetalingsStatus.UTBETALT || it.status == UtbetalingsStatus.ANNULLERT) }
                                        .map { utbetaling ->
                                            ManedUtbetaling(
                                                    tittel = utbetaling.beskrivelse,
                                                    belop = utbetaling.belop.toDouble(),
                                                    utbetalingsdato = utbetaling.utbetalingsDato,
                                                    status = utbetaling.status.name,
                                                    fiksDigisosId = digisosSak.fiksDigisosId,
                                                    fom = utbetaling.fom,
                                                    tom = utbetaling.tom,
                                                    mottaker = utbetaling.mottaker,
                                                    harVilkar = !utbetaling.vilkar.isNullOrEmpty()
                                            )
                                        }
                            }
                }

        return alleUtbetalinger
                .sortedByDescending { it.utbetalingsdato}
                .groupBy { YearMonth.of(it.utbetalingsdato!!.year, it.utbetalingsdato.month) }
                .map { (key, value) ->
                    UtbetalingerResponse(
                            ar = key.year,
                            maned = monthToString(key.monthValue),
                            sum = value.filter { it.status == UtbetalingsStatus.UTBETALT.name }.sumByDouble { it.belop },
                            utbetalinger = value.sortedByDescending { it.utbetalingsdato }
                    )
                }
    }

    private fun monthToString(month: Int) = DateFormatSymbols().months[month - 1]

    companion object {
        val log by logger()
    }

}