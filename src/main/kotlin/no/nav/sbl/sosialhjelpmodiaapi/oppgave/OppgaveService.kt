package no.nav.sbl.sosialhjelpmodiaapi.oppgave

import no.nav.sbl.sosialhjelpmodiaapi.domain.Oppgave
import no.nav.sbl.sosialhjelpmodiaapi.domain.OppgaveResponse
import no.nav.sbl.sosialhjelpmodiaapi.domain.OpplastetDokument
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.vedlegg.VedleggService
import no.nav.sbl.sosialhjelpmodiaapi.vedlegg.VedleggService.InternalVedlegg
import org.springframework.stereotype.Component


@Component
class OppgaveService(private val fiksClient: FiksClient,
                     private val eventService: EventService,
                     private val vedleggService: VedleggService) {

    fun hentOppgaver(fiksDigisosId: String, token: String): List<OppgaveResponse> {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId, token)
        val model = eventService.createModel(digisosSak, token)

        if (model.oppgaver.isEmpty()) {
            return emptyList()
        }

        val ettersendteVedlegg = vedleggService.hentEttersendteVedlegg(fiksDigisosId, model, digisosSak.ettersendtInfoNAV, token)

        val oppgaveResponseList = model.oppgaver
                .sortedBy { it.innsendelsesfrist }
                .map {
                    OppgaveResponse(
                            innsendelsesfrist = it.innsendelsesfrist,
                            dokumenttype = it.tittel,
                            tilleggsinformasjon = it.tilleggsinfo,
                            datoLagtTil = it.tidspunktForKrav,
                            dokumenterLastetOpp = hentOpplastedeDokumenter(it, ettersendteVedlegg),
                            erFraInnsyn = it.erFraInnsyn)
                }
        log.info("Hentet ${oppgaveResponseList.size} oppgaver for fiksDigisosId=$fiksDigisosId")
        return oppgaveResponseList
    }

    private fun hentOpplastedeDokumenter(oppgave: Oppgave, vedleggListe: List<InternalVedlegg>): List<OpplastetDokument> {
        return vedleggListe
                .filter { it.type == oppgave.tittel && it.tilleggsinfo == oppgave.tilleggsinfo}
                .filter { it.datoLagtTil.isAfter(oppgave.tidspunktForKrav) } // lastet opp _etter_ krav ble utstedt
                .map {
                    OpplastetDokument(
                            it.datoLagtTil,
                            it.type,
                            it.tilleggsinfo
                    )
                }
    }

    companion object {
        val log by logger()
    }

}