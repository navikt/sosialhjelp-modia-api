package no.nav.sbl.sosialhjelpmodiaapi.service.utbetalinger

import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.NavKontor
import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingerResponse
import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingsStatus
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.springframework.stereotype.Component


@Component
class UtbetalingerService(
        private val fiksClient: FiksClient,
        private val eventService: EventService
) {

    fun hentUtbetalinger(token: String, fnr: String): List<UtbetalingerResponse> {
        val digisosSaker = fiksClient.hentAlleDigisosSaker(token, fnr)

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
        val behandlendeNavKontor = model.navKontorHistorikk.lastOrNull()

        return model.saker
                .flatMap { sak ->
                    sak.utbetalinger
                            .filter { it.status != UtbetalingsStatus.ANNULLERT && (it.utbetalingsDato != null || it.forfallsDato != null) }
                            .map { utbetaling ->
                                UtbetalingerResponse(
                                        tittel = utbetaling.beskrivelse,
                                        belop = utbetaling.belop.toDouble(),
                                        utbetalingEllerForfallDigisosSoker = utbetaling.utbetalingsDato
                                                ?: utbetaling.forfallsDato,
                                        status = utbetaling.status,
                                        fiksDigisosId = digisosSak.fiksDigisosId,
                                        fom = utbetaling.fom,
                                        tom = utbetaling.tom,
                                        mottaker = utbetaling.mottaker,
                                        kontonummer = utbetaling.kontonummer,
                                        utbetalingsmetode = utbetaling.utbetalingsmetode,
                                        harVilkar = !utbetaling.vilkar.isNullOrEmpty(),
                                        navKontor = behandlendeNavKontor?.let { NavKontor(it.navEnhetsnavn, it.navEnhetsnummer) }
                                )
                            }
                }
    }

    companion object {
        private val log by logger()
    }
}