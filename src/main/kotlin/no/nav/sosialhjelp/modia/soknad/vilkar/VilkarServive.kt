package no.nav.sosialhjelp.modia.soknad.vilkar

import no.nav.sosialhjelp.modia.digisossak.domain.OppgaveStatus
import no.nav.sosialhjelp.modia.digisossak.domain.Utbetaling
import no.nav.sosialhjelp.modia.digisossak.event.EventService
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksClient
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.soknad.dokumentasjonkrav.DOKUMENTASJONKRAV_UTEN_SAK_TITTEL
import no.nav.sosialhjelp.modia.soknad.dokumentasjonkrav.hentSakstittel
import org.springframework.stereotype.Component

@Component
class VilkarServive(
    private val fiksClient: FiksClient,
    private val eventService: EventService,
) {
    fun hentVilkar(fiksDigisosId: String): List<VilkarResponse> {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)
        val model = eventService.createModel(digisosSak)
        if (model.vilkar.isEmpty()) {
            return emptyList()
        }

        val vilkarResponseList = model.vilkar
            .filter { it.status == OppgaveStatus.RELEVANT }
            .map {
                VilkarResponse(
                    referanse = it.referanse,
                    sakstittel = hentSakstittel(it.saksreferanse, model.saker),
                    utbetalingsreferanse = hentUtbetalingsreferanse(it.utbetalingsReferanse, model.utbetalinger),
                    datoLagtTil = it.datoLagtTil.toLocalDate()
                )
            }
            .sortedBy { it.datoLagtTil }

        log.info("Hentet ${vilkarResponseList.size} vilkar")
        return vilkarResponseList
    }

    private fun hentUtbetalingsreferanse(utbetalingsReferanse: List<String>, utbetalinger: MutableList<Utbetaling>): List<String> {
        return utbetalingsReferanse
            .map { hentUtbetaling(utbetalinger, it)?.beskrivelse ?: DOKUMENTASJONKRAV_UTEN_SAK_TITTEL }
    }

    private fun hentUtbetaling(utbetalinger: MutableList<Utbetaling>, utbetalingsReferanse: String): Utbetaling? {
        return utbetalinger
            .firstOrNull { it.referanse == utbetalingsReferanse }
    }

    companion object {
        private val log by logger()
    }
}
