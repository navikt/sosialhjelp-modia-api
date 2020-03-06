package no.nav.sbl.sosialhjelpmodiaapi.utbetalinger

import no.nav.sbl.sosialhjelpmodiaapi.domain.DigisosSak
import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingerResponse
import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingsStatus
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.springframework.stereotype.Component


@Component
class UtbetalingerService(private val fiksClient: FiksClient,
                          private val eventService: EventService) {

    fun hentUtbetalinger(token: String): List<UtbetalingerResponse> {
        val digisosSaker = fiksClient.hentAlleDigisosSaker(token)

        if (digisosSaker.isEmpty()) {
            log.info("Fant ingen søknader for bruker")
            return emptyList()
        }

        return digisosSaker
                .flatMap { digisosSak -> utbetalingerForDigisosSak(digisosSak, token) }
                .sortedByDescending { it.utbetalingEllerForfallDigisosSoker }
    }

    fun hentUtbetalingerForDigisosSak(digisosSak: DigisosSak, token: String): List<UtbetalingerResponse> {
        return utbetalingerForDigisosSak(digisosSak, token)
                .sortedByDescending { it.utbetalingEllerForfallDigisosSoker }
    }

    private fun utbetalingerForDigisosSak(digisosSak: DigisosSak, token: String): List<UtbetalingerResponse> {
        val model = eventService.createModel(digisosSak, token)
        return model.saker
                .flatMap { sak ->
                    sak.utbetalinger
                            .filter { it.status != UtbetalingsStatus.ANNULLERT && (it.utbetalingsDato != null || it.forfallsDato != null) }
                            .map { utbetaling ->
                                UtbetalingerResponse(
                                        tittel = utbetaling.beskrivelse,
                                        belop = utbetaling.belop.toDouble(),
                                        utbetalingEllerForfallDigisosSoker = utbetaling.utbetalingsDato ?: utbetaling.forfallsDato,
                                        status = utbetaling.status.name,
                                        fiksDigisosId = digisosSak.fiksDigisosId,
                                        fom = utbetaling.fom,
                                        tom = utbetaling.tom,
                                        mottaker = utbetaling.mottaker,
                                        kontonummer = utbetaling.kontonummer,
                                        utbetalingsmetode = utbetaling.utbetalingsmetode,
                                        harVilkar = !utbetaling.vilkar.isNullOrEmpty()
                                )
                            }
                }
    }

    companion object {
        val log by logger()
    }

}