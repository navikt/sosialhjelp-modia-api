package no.nav.sosialhjelp.modia.service.dokumentasjonkrav

import no.nav.sosialhjelp.modia.client.fiks.FiksClient
import no.nav.sosialhjelp.modia.domain.Dokumentasjonkrav
import no.nav.sosialhjelp.modia.domain.OppgaveStatus
import no.nav.sosialhjelp.modia.event.EventService
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.rest.DokumentasjonkravController.DokumentasjonkravResponse
import no.nav.sosialhjelp.modia.service.vedlegg.InternalVedlegg
import no.nav.sosialhjelp.modia.service.vedlegg.VedleggService
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DokumentasjonkravService(
    private val fiksClient: FiksClient,
    private val eventService: EventService,
    private val vedleggService: VedleggService
) {
    fun hentDokumentasjonkrav(fiksDigiosId: String): List<DokumentasjonkravResponse> {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigiosId)
        val model = eventService.createModel(digisosSak)

        if (model.dokumentasjonkrav.isEmpty()) {
            return emptyList()
        }
        val ettersendteVedlegg = vedleggService.hentEttersendteVedlegg(digisosSak, model)

        val dokumentasjonkravResponseList = model.dokumentasjonkrav
            .asSequence()
            .filter {
                !it.isEmpty()
                    .also { isEmpty -> if (isEmpty) log.error("Tittel og beskrivelse på dokumentasjonkrav er tomt") }
            }
            .filter { !erAlleredeLastetOpp(it, ettersendteVedlegg) }
            .filter { it.status == OppgaveStatus.RELEVANT }
            .groupBy { it.frist }
            .map { (_, value) ->
                DokumentasjonkravResponse(
                    referanse = value[0].dokumentasjonkravId,
                    sakstittel = "sakstittel",
                    status = value[0].status.toString(),
                    utbetalingsbeskrivelse = "utebetalingsbeskrivelse",
                    innsendelsesfrist = value[0].frist?.toLocalDate(),
                    vedleggDatoLagtTil = hentVedleggDatoLagtTil(value[0], ettersendteVedlegg),
                    antallVedlegg = hentAntallOpplastedeVedlegg(value[0], ettersendteVedlegg)
                )
            }
            .toList()
        log.info("Hentet ${dokumentasjonkravResponseList.size} dokumentasjonkrav")
        return dokumentasjonkravResponseList
    }

    private fun hentAntallOpplastedeVedlegg(dokumentasjonkrav: Dokumentasjonkrav, vedleggListe: List<InternalVedlegg>): Int {
        return vedleggListe
            .filter { it.type == dokumentasjonkrav.tittel && it.tilleggsinfo == dokumentasjonkrav.beskrivelse }
            .firstOrNull { it.datoLagtTil != null && it.datoLagtTil.isAfter(dokumentasjonkrav.tidspunktForKrav) }
            ?.antallFiler ?: 0
    }

    // FIXme: flere enn 1 vedlegg lastet opp på ulike datoer. Bruk nyeste dato?
    //  skal kanskje ikke være mulig, siden oppgaver i innsyn ansees som ferdige ved opplasting?
    private fun hentVedleggDatoLagtTil(dokumentasjonkrav: Dokumentasjonkrav, vedleggListe: List<InternalVedlegg>): LocalDate? {
        return vedleggListe
            .filter { it.type == dokumentasjonkrav.tittel && it.tilleggsinfo == dokumentasjonkrav.beskrivelse }
            .filter { it.datoLagtTil != null && it.datoLagtTil.isAfter(dokumentasjonkrav.tidspunktForKrav) }
            .maxByOrNull { it.datoLagtTil!! }
            ?.datoLagtTil?.toLocalDate()
    }

    private fun erAlleredeLastetOpp(dokumentasjonkrav: Dokumentasjonkrav, vedleggListe: List<InternalVedlegg>): Boolean {
        return vedleggListe
            .filter { it.type == dokumentasjonkrav.tittel }
            .filter { it.tilleggsinfo == dokumentasjonkrav.beskrivelse }
            .any { dokumentasjonkrav.frist == null || it.tidspunktLastetOpp.isAfter(dokumentasjonkrav.datoLagtTil) }
    }

    companion object {
        private val log by logger()
    }
}