package no.nav.sosialhjelp.modia.service.utbetalinger

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.modia.client.fiks.FiksClient
import no.nav.sosialhjelp.modia.domain.NavKontor
import no.nav.sosialhjelp.modia.domain.NavKontorInformasjon
import no.nav.sosialhjelp.modia.domain.Utbetaling
import no.nav.sosialhjelp.modia.domain.UtbetalingsStatus
import no.nav.sosialhjelp.modia.event.EventService
import no.nav.sosialhjelp.modia.flatMapParallel
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.rest.UtbetalingerController.UtbetalingerResponse
import no.nav.sosialhjelp.modia.unixToLocalDateTime
import org.joda.time.DateTime
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder.getRequestAttributes
import org.springframework.web.context.request.RequestContextHolder.setRequestAttributes
import java.time.LocalDate


@Component
class UtbetalingerService(
        private val fiksClient: FiksClient,
        private val eventService: EventService
) {

    fun hentAlleUtbetalinger(fnr: String, months: Int, fom: LocalDate?, tom: LocalDate?): List<UtbetalingerResponse> {
        val digisosSaker = fiksClient.hentAlleDigisosSaker(fnr)

        if (digisosSaker.isEmpty()) {
            log.info("Fant ingen søknader for bruker")
            return emptyList()
        }

        return when {
            fom == null && tom == null -> utbetalingerSisteManeder(digisosSaker, months)
            else -> hentUtbetalingerForIntervall(digisosSaker, fom, tom)
        }
    }

    fun hentUtbetalingerForDigisosSak(digisosSak: DigisosSak): List<UtbetalingerResponse> {
        return getUtbetalinger(digisosSak)
                .sortedByDescending { it.utbetalingEllerForfallDigisosSoker }
    }

    private fun utbetalingerSisteManeder(digisosSaker: List<DigisosSak>, months: Int): List<UtbetalingerResponse> {
        val requestAttributes = getRequestAttributes()

        return runBlocking(Dispatchers.IO + MDCContext()) {
            digisosSaker
                    .filter { isDigisosSakNewerThanMonths(it, months) }
                    .flatMapParallel {
                        setRequestAttributes(requestAttributes)
                        getUtbetalinger(it)
                    }
                    .sortedByDescending { it.utbetalingEllerForfallDigisosSoker }
        }
    }

    private fun hentUtbetalingerForIntervall(digisosSaker: List<DigisosSak>, fom: LocalDate?, tom: LocalDate?): List<UtbetalingerResponse> {
        val requestAttributes = getRequestAttributes()

        return runBlocking(Dispatchers.IO + MDCContext()) {
            digisosSaker
                    .filter { isDigisosSakInnenforIntervall(it, fom, tom) }
                    .flatMapParallel {
                        setRequestAttributes(requestAttributes)
                        getUtbetalinger(it)
                    }
                    .sortedByDescending { it.utbetalingEllerForfallDigisosSoker }
        }
    }

    private fun isDigisosSakNewerThanMonths(digisosSak: DigisosSak, months: Int): Boolean {
        return digisosSak.sistEndret >= DateTime.now().minusMonths(months).millis
    }

    private fun isDigisosSakInnenforIntervall(digisosSak: DigisosSak, fom: LocalDate?, tom: LocalDate?): Boolean {
        val range = when {
            fom != null && tom != null && fom.isBefore(tom) -> fom.rangeTo(tom)
            fom != null && tom != null && fom.isAfter(tom) -> throw IllegalStateException("Fom kan ikke være etter tom")
            fom != null && tom == null -> fom.rangeTo(LocalDate.now())
            fom == null && tom != null -> LocalDate.now().minusYears(1).rangeTo(tom)
            else -> throw IllegalStateException("Fom og tom kan ikke begge være null")
        }
        return range.contains(unixToLocalDateTime(digisosSak.sistEndret).toLocalDate())
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