package no.nav.sosialhjelp.modia.soknad.oppgave

import no.nav.sosialhjelp.modia.digisossak.domain.Oppgave
import no.nav.sosialhjelp.modia.digisossak.event.EventService
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksClient
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.soknad.vedlegg.InternalVedlegg
import no.nav.sosialhjelp.modia.soknad.vedlegg.VedleggService
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class OppgaveService(
    private val fiksClient: FiksClient,
    private val eventService: EventService,
    private val vedleggService: VedleggService,
) {
    suspend fun hentOppgaver(fiksDigisosId: String): List<OppgaveResponse> {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)
        val model = eventService.createModel(digisosSak)

        if (model.oppgaver.isEmpty()) {
            return emptyList()
        }

        val ettersendteVedlegg = vedleggService.hentEttersendteVedlegg(digisosSak, model)

        val oppgaveResponseList =
            model.oppgaver
                .sortedBy { it.innsendelsesfrist }
                .map {
                    OppgaveResponse(
                        dokumenttype = it.tittel,
                        tilleggsinformasjon = it.tilleggsinfo,
                        innsendelsesfrist = it.innsendelsesfrist?.toLocalDate(),
                        vedleggDatoLagtTil = hentVedleggDatoLagtTil(it, ettersendteVedlegg),
                        antallVedlegg = hentAntallOpplastedeVedlegg(it, ettersendteVedlegg),
                        erFraInnsyn = it.erFraInnsyn,
                    )
                }.filter { it.antallVedlegg < 1 }
        log.info("Hentet ${oppgaveResponseList.size} oppgaver for fiksDigisosId=$fiksDigisosId")
        return oppgaveResponseList
    }

    private fun hentAntallOpplastedeVedlegg(
        oppgave: Oppgave,
        vedleggListe: List<InternalVedlegg>,
    ): Int =
        vedleggListe
            .filter { it.type == oppgave.tittel && it.tilleggsinfo == oppgave.tilleggsinfo }
            .firstOrNull { it.datoLagtTil != null && it.datoLagtTil.isAfter(oppgave.tidspunktForKrav) }
            ?.antallFiler ?: 0

    // FIXme: flere enn 1 vedlegg lastet opp på ulike datoer. Bruk nyeste dato?
    //  skal kanskje ikke være mulig, siden oppgaver i innsyn ansees som ferdige ved opplasting?
    private fun hentVedleggDatoLagtTil(
        oppgave: Oppgave,
        vedleggListe: List<InternalVedlegg>,
    ): LocalDate? =
        vedleggListe
            .filter { it.type == oppgave.tittel && it.tilleggsinfo == oppgave.tilleggsinfo }
            .filter { it.datoLagtTil != null && it.datoLagtTil.isAfter(oppgave.tidspunktForKrav) }
            .maxByOrNull { it.datoLagtTil!! }
            ?.datoLagtTil
            ?.toLocalDate()

    companion object {
        private val log by logger()
    }
}
