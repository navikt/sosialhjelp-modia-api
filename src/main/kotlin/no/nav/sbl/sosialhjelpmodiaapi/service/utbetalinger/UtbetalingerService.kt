package no.nav.sbl.sosialhjelpmodiaapi.service.utbetalinger

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.NavKontor
import no.nav.sbl.sosialhjelpmodiaapi.domain.NavKontorInformasjon
import no.nav.sbl.sosialhjelpmodiaapi.domain.Utbetaling
import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingerResponse
import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingsStatus
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.flatMapParallel
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.utils.coroutines.RequestContextService
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.joda.time.DateTime
import org.springframework.stereotype.Component


@Component
class UtbetalingerService(
        private val fiksClient: FiksClient,
        private val eventService: EventService,
        private val requestContextService: RequestContextService
) {

    fun hentAlleUtbetalinger(fnr: String, months: Int): List<UtbetalingerResponse> {
        val digisosSaker = fiksClient.hentAlleDigisosSaker(fnr)

        if (digisosSaker.isEmpty()) {
            log.info("Fant ingen søknader for bruker")
            return emptyList()
        }

        return runBlocking(context = requestContextService.getCoroutineContext()) {
            digisosSaker
                    .filter { isDigisosSakNewerThanMonths(it, months) }
                    .flatMapParallel { getUtbetalinger(it) }
                    .sortedByDescending { it.utbetalingEllerForfallDigisosSoker }
        }
    }

    fun hentUtbetalingerForDigisosSak(digisosSak: DigisosSak): List<UtbetalingerResponse> {
        return getUtbetalinger(digisosSak)
                .sortedByDescending { it.utbetalingEllerForfallDigisosSoker }
    }

    fun isDigisosSakNewerThanMonths(digisosSak: DigisosSak, months: Int): Boolean {
        return digisosSak.sistEndret >= DateTime.now().minusMonths(months).millis
    }

    private fun getUtbetalinger(digisosSak: DigisosSak): List<UtbetalingerResponse> {
        val model = eventService.createModel(digisosSak)
        val behandlendeNavKontor = model.navKontorHistorikk.lastOrNull()

        return model.utbetalinger
                .filter { it.status != UtbetalingsStatus.ANNULLERT && (it.utbetalingsDato != null || it.forfallsDato != null) }
                .map { utbetaling ->
                    utbetaling.infoLoggVedManglendeUtbetalingsDatoEllerForfallsDato(digisosSak.kommunenummer)
                    toUtbetalingResponse(utbetaling, digisosSak.fiksDigisosId, behandlendeNavKontor)
                }
    }

    private fun toUtbetalingResponse(utbetaling: Utbetaling, fiksDigisosId: String, behandlendeNavKontor: NavKontorInformasjon?): UtbetalingerResponse {
        return UtbetalingerResponse(
                tittel = utbetaling.beskrivelse,
                belop = utbetaling.belop.toDouble(),
                utbetalingEllerForfallDigisosSoker = utbetaling.utbetalingsDato
                        ?: utbetaling.forfallsDato,
                status = utbetaling.status,
                fiksDigisosId = fiksDigisosId,
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

    private fun Utbetaling.infoLoggVedManglendeUtbetalingsDatoEllerForfallsDato(kommunenummer: String) {
        when {
            status == UtbetalingsStatus.UTBETALT && utbetalingsDato == null -> {
                log.info("Utbetaling ($referanse) med status=${UtbetalingsStatus.UTBETALT} har ikke utbetalingsDato. Kommune=$kommunenummer")
            }
            status == UtbetalingsStatus.PLANLAGT_UTBETALING && forfallsDato == null -> {
                log.info("Utbetaling ($referanse) med status=${UtbetalingsStatus.PLANLAGT_UTBETALING} har ikke forfallsDato. Kommune=$kommunenummer")
            }
            status == UtbetalingsStatus.STOPPET && (forfallsDato == null || utbetalingsDato == null) -> {
                log.info("Utbetaling ($referanse) med status=${UtbetalingsStatus.STOPPET} mangler forfallsDato eller utbetalingsDato. Kommune=$kommunenummer")
            }
        }
    }

    companion object {
        private val log by logger()
    }
}