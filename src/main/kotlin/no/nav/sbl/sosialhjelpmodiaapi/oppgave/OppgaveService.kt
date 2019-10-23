package no.nav.sbl.sosialhjelpmodiaapi.oppgave

import no.nav.sbl.sosialhjelpmodiaapi.domain.OppgaveResponse
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.springframework.stereotype.Component


@Component
class OppgaveService(private val eventService: EventService,
                     private val fiksClient: FiksClient) {

    companion object {
        val log by logger()
    }

    fun hentOppgaver(fiksDigisosId: String, token: String): List<OppgaveResponse> {
        val model = eventService.createModel(fiksDigisosId, token)

        if (model.oppgaver.isEmpty()) {
            return emptyList()
        }

        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId, token)

        val oppgaveResponseList = model.oppgaver.sortedBy { it.innsendelsesfrist }
                .map {
                    OppgaveResponse(
                            if (it.innsendelsesfrist == null) null else it.innsendelsesfrist.toString(),
                            it.tittel,
                            it.tilleggsinfo,
                            it.erFraInnsyn)
                }
        log.info("Hentet ${oppgaveResponseList.size} oppgaver for fiksDigisosId=$fiksDigisosId")
        return oppgaveResponseList
    }

}