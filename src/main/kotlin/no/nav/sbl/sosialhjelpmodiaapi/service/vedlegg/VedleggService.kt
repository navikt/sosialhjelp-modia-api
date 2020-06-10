package no.nav.sbl.sosialhjelpmodiaapi.service.vedlegg

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.unixToLocalDateTime
import no.nav.sosialhjelp.api.fiks.DokumentInfo
import no.nav.sosialhjelp.api.fiks.EttersendtInfoNAV
import no.nav.sosialhjelp.api.fiks.OriginalSoknadNAV
import org.springframework.stereotype.Component
import java.time.LocalDateTime

const val LASTET_OPP_STATUS = "LastetOpp"
const val VEDLEGG_KREVES_STATUS = "VedleggKreves"

@Component
class VedleggService(
        private val fiksClient: FiksClient,
        private val eventService: EventService
) {

    fun hentAlleOpplastedeVedlegg(fiksDigisosId: String): List<InternalVedlegg> {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)
        val model = eventService.createModel(digisosSak)

        val soknadVedlegg = hentSoknadVedleggMedStatus(LASTET_OPP_STATUS, fiksDigisosId, digisosSak.originalSoknadNAV)
        val ettersendteVedlegg = hentEttersendteVedlegg(fiksDigisosId, model, digisosSak.ettersendtInfoNAV)

        val utestaendeOppgaver = hentUtestaendeOppgaverSomManglendeVedlegg(model, ettersendteVedlegg)

        return soknadVedlegg.plus(ettersendteVedlegg).plus(utestaendeOppgaver)
    }

    fun hentSoknadVedleggMedStatus(status: String, fiksDigisosId: String, originalSoknadNAV: OriginalSoknadNAV?): List<InternalVedlegg> {
        if (originalSoknadNAV == null) {
            return emptyList()
        }
        val jsonVedleggSpesifikasjon = hentVedleggSpesifikasjon(fiksDigisosId, originalSoknadNAV.vedleggMetadata)

        if (jsonVedleggSpesifikasjon.vedlegg.isEmpty()) {
            return emptyList()
        }

        return jsonVedleggSpesifikasjon.vedlegg
                .filter { vedlegg -> vedlegg.status == status }
                .map { vedlegg ->
                    InternalVedlegg(
                            type = vedlegg.type,
                            tilleggsinfo = vedlegg.tilleggsinfo,
                            innsendelsesfrist = null,
                            antallFiler = matchDokumentInfoOgJsonFiler(originalSoknadNAV.vedlegg, vedlegg.filer),
                            datoLagtTil = unixToLocalDateTime(originalSoknadNAV.timestampSendt)
                    )
                }
    }

    fun hentEttersendteVedlegg(fiksDigisosId: String, model: InternalDigisosSoker, ettersendtInfoNAV: EttersendtInfoNAV?): List<InternalVedlegg> {
        return ettersendtInfoNAV?.ettersendelser
                ?.flatMap { ettersendelse ->
                    val jsonVedleggSpesifikasjon = hentVedleggSpesifikasjon(fiksDigisosId, ettersendelse.vedleggMetadata)
                    jsonVedleggSpesifikasjon.vedlegg
                            .filter { vedlegg -> LASTET_OPP_STATUS == vedlegg.status }
                            .map { vedlegg ->
                                InternalVedlegg(
                                        type = vedlegg.type,
                                        tilleggsinfo = vedlegg.tilleggsinfo,
                                        innsendelsesfrist = hentInnsendelsesfristFraOppgave(model, vedlegg),
                                        antallFiler = matchDokumentInfoOgJsonFiler(ettersendelse.vedlegg, vedlegg.filer),
                                        datoLagtTil = unixToLocalDateTime(ettersendelse.timestampSendt)
                                )
                            }
                } ?: emptyList()
    }

    private fun hentUtestaendeOppgaverSomManglendeVedlegg(model: InternalDigisosSoker, ettersendteVedlegg: List<InternalVedlegg>): List<InternalVedlegg> {
        return model.oppgaver
                .filterNot { oppgave ->
                    ettersendteVedlegg
                            .any { it.type == oppgave.tittel && it.tilleggsinfo == oppgave.tilleggsinfo && it.innsendelsesfrist == oppgave.innsendelsesfrist }
                }
                .map {
                    InternalVedlegg(
                            type = it.tittel,
                            tilleggsinfo = it.tilleggsinfo,
                            innsendelsesfrist = it.innsendelsesfrist,
                            antallFiler = 0,
                            datoLagtTil = null
                    )
                }
    }

    private fun hentVedleggSpesifikasjon(fiksDigisosId: String, dokumentlagerId: String): JsonVedleggSpesifikasjon {
        return fiksClient.hentDokument(fiksDigisosId, dokumentlagerId, JsonVedleggSpesifikasjon::class.java) as JsonVedleggSpesifikasjon
    }

    private fun matchDokumentInfoOgJsonFiler(dokumentInfoList: List<DokumentInfo>, jsonFiler: List<JsonFiler>): Int {
        return jsonFiler
                .flatMap { fil ->
                    dokumentInfoList
                            .filter { it.filnavn == fil.filnavn }
                }.count()
    }

    private fun hentInnsendelsesfristFraOppgave(model: InternalDigisosSoker, vedlegg: JsonVedlegg): LocalDateTime? {
        return model.oppgaver
                .sortedByDescending { it.innsendelsesfrist }
                .firstOrNull { it.tittel == vedlegg.type && it.tilleggsinfo == vedlegg.tilleggsinfo }
                ?.innsendelsesfrist
    }

    data class InternalVedlegg(
            val type: String,
            val tilleggsinfo: String?,
            val innsendelsesfrist: LocalDateTime?,
            val antallFiler: Int,
            val datoLagtTil: LocalDateTime?
    )
}