package no.nav.sbl.sosialhjelpmodiaapi.oppgave

import no.nav.sbl.sosialhjelpmodiaapi.domain.Oppgave
import no.nav.sbl.sosialhjelpmodiaapi.domain.OppgaveResponse
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.vedlegg.VedleggService
import no.nav.sbl.sosialhjelpmodiaapi.vedlegg.VedleggService.InternalVedlegg
import org.springframework.stereotype.Component
import java.time.LocalDate


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
                            dokumenttype = it.tittel,
                            tilleggsinformasjon = it.tilleggsinfo,
                            innsendelsesfrist = it.innsendelsesfrist?.toLocalDate(),
                            vedleggDatoLagtTil = hentVedleggDatoLagtTil(it, ettersendteVedlegg),
                            antallVedlegg = hentAntallOpplastedeVedlegg(it, ettersendteVedlegg),
                            erFraInnsyn = it.erFraInnsyn)
                }
        log.info("Hentet ${oppgaveResponseList.size} oppgaver for fiksDigisosId=$fiksDigisosId")
        return oppgaveResponseList
    }

    private fun hentAntallOpplastedeVedlegg(oppgave: Oppgave, vedleggListe: List<InternalVedlegg>): Int {
        return vedleggListe
                .filter { it.type == oppgave.tittel && it.tilleggsinfo == oppgave.tilleggsinfo }
                .firstOrNull { it.datoLagtTil != null && it.datoLagtTil.isAfter(oppgave.tidspunktForKrav) }
                ?.antallFiler ?: 0
    }

    // FIXme: flere enn 1 vedlegg lastet opp på ulike datoer. Bruk nyeste dato?
    //  skal kanskje ikke være mulig, siden oppgaver i innsyn ansees som ferdige ved opplasting?
    private fun hentVedleggDatoLagtTil(oppgave: Oppgave, vedleggListe: List<InternalVedlegg>): LocalDate? {
        return vedleggListe
                .filter { it.type == oppgave.tittel && it.tilleggsinfo == oppgave.tilleggsinfo }
                .filter { it.datoLagtTil != null && it.datoLagtTil.isAfter(oppgave.tidspunktForKrav) }
                .maxBy { it.datoLagtTil!! }
                ?.datoLagtTil?.toLocalDate()
    }

    companion object {
        private val log by logger()
    }

}