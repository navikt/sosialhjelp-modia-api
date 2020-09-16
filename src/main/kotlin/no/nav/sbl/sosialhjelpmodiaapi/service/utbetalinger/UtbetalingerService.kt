package no.nav.sbl.sosialhjelpmodiaapi.service.utbetalinger

import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.NavKontor
import no.nav.sbl.sosialhjelpmodiaapi.domain.Utbetaling
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

    fun hentUtbetalinger(fnr: String): List<UtbetalingerResponse> {
        val digisosSaker = fiksClient.hentAlleDigisosSaker(fnr)

        if (digisosSaker.isEmpty()) {
            log.info("Fant ingen søknader for bruker")
            return emptyList()
        }

        return digisosSaker
                .flatMap { digisosSak -> utbetalingerForDigisosSak(digisosSak) }
                .sortedByDescending { it.utbetalingEllerForfallDigisosSoker }
    }

    fun hentUtbetalingerForDigisosSak(digisosSak: DigisosSak): List<UtbetalingerResponse> {
        return utbetalingerForDigisosSak(digisosSak)
                .sortedByDescending { it.utbetalingEllerForfallDigisosSoker }
    }

    private fun utbetalingerForDigisosSak(digisosSak: DigisosSak): List<UtbetalingerResponse> {
        val model = eventService.createModel(digisosSak)
        val behandlendeNavKontor = model.navKontorHistorikk.lastOrNull()

        return model.saker
                .flatMap { sak ->
                    infoLoggVedManglendeUtbetalingsDatoEllerForfallsDato(sak.utbetalinger, digisosSak.kommunenummer)

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
                                        annenMottaker = utbetaling.annenMottaker,
                                        kontonummer = utbetaling.kontonummer,
                                        utbetalingsmetode = utbetaling.utbetalingsmetode,
                                        harVilkar = !utbetaling.vilkar.isNullOrEmpty(),
                                        navKontor = behandlendeNavKontor?.let { NavKontor(it.navEnhetsnavn, it.navEnhetsnummer) }
                                )
                            }
                }
    }

    private fun infoLoggVedManglendeUtbetalingsDatoEllerForfallsDato(utbetalinger: List<Utbetaling>, kommunenummer: String) {
        utbetalinger
                .filter { it.status == UtbetalingsStatus.UTBETALT && it.utbetalingsDato == null }
                .forEach { log.info("Utbetaling (${it.referanse}) med status=${UtbetalingsStatus.UTBETALT} har ikke utbetalingsDato. Kommune=$kommunenummer") }

        utbetalinger
                .filter { it.status == UtbetalingsStatus.PLANLAGT_UTBETALING && it.forfallsDato == null }
                .forEach { log.info("Utbetaling (${it.referanse}) med status=${UtbetalingsStatus.PLANLAGT_UTBETALING} har ikke forfallsDato. Kommune=$kommunenummer") }

        utbetalinger
                .filter { it.status == UtbetalingsStatus.STOPPET && (it.forfallsDato == null || it.utbetalingsDato == null) }
                .forEach { log.info("Utbetaling (${it.referanse}) med status=${UtbetalingsStatus.STOPPET} mangler forfallsDato eller utbetalingsDato. Kommune=$kommunenummer") }
    }

    companion object {
        private val log by logger()
    }
}