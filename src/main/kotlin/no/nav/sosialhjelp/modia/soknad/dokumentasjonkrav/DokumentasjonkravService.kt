package no.nav.sosialhjelp.modia.service.dokumentasjonkrav

import no.nav.sosialhjelp.modia.client.fiks.FiksClient
import no.nav.sosialhjelp.modia.domain.Dokumentasjonkrav
import no.nav.sosialhjelp.modia.domain.OppgaveStatus
import no.nav.sosialhjelp.modia.domain.Sak
import no.nav.sosialhjelp.modia.event.EventService
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.rest.DokumentasjonkravController.DokumentasjonkravResponse
import no.nav.sosialhjelp.modia.service.vedlegg.InternalVedlegg
import no.nav.sosialhjelp.modia.service.vedlegg.VedleggService
import org.springframework.stereotype.Component

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
                    sakstittel = hentSakstittel(value[0].saksreferanse, model.saker),
                    status = value[0].status.toString(),
                    antallVedlegg = hentAntallOpplastedeVedlegg(value[0], ettersendteVedlegg),
                    innsendelsesfrist = value[0].frist?.toLocalDate(),
                    datoLagtTil = value[0].datoLagtTil?.toLocalDate()
                )
            }
            .toList()
        log.info("Hentet ${dokumentasjonkravResponseList.size} dokumentasjonkrav")
        return dokumentasjonkravResponseList
    }

    private fun hentSakstittel(saksreferanse: String?, saker: MutableList<Sak>): String {
        if (saksreferanse == null) {
            return "―"
        }
        return saker.firstOrNull { sak -> sak.referanse == saksreferanse }?.tittel ?: "―"
    }

    private fun hentAntallOpplastedeVedlegg(dokumentasjonkrav: Dokumentasjonkrav, vedleggListe: List<InternalVedlegg>): Int {
        return vedleggListe
            .filter { it.type == dokumentasjonkrav.tittel && it.tilleggsinfo == dokumentasjonkrav.beskrivelse }
            .firstOrNull { it.datoLagtTil != null && it.datoLagtTil.isAfter(dokumentasjonkrav.datoLagtTil) }
            ?.antallFiler ?: 0
    }

    private fun erAlleredeLastetOpp(dokumentasjonkrav: Dokumentasjonkrav, vedleggListe: List<InternalVedlegg>): Boolean {
        return vedleggListe
            .filter { it.type == dokumentasjonkrav.tittel }
            .filter { it.tilleggsinfo == dokumentasjonkrav.beskrivelse }
            .any { dokumentasjonkrav.frist == null || it.tidspunktLastetOpp?.isAfter(dokumentasjonkrav.datoLagtTil) ?: false }
    }

    companion object {
        private val log by logger()
    }
}
