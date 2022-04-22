package no.nav.sosialhjelp.modia.soknad.vedlegg

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.modia.client.fiks.FiksClient
import no.nav.sosialhjelp.modia.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.event.EventService
import no.nav.sosialhjelp.modia.flatMapParallel
import no.nav.sosialhjelp.modia.unixToLocalDateTime
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder.getRequestAttributes
import org.springframework.web.context.request.RequestContextHolder.setRequestAttributes
import java.time.LocalDateTime

@Component
class VedleggService(
    private val fiksClient: FiksClient,
    private val eventService: EventService,
    private val soknadVedleggService: SoknadVedleggService
) {

    fun hentAlleOpplastedeVedlegg(fiksDigisosId: String): List<InternalVedlegg> {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)
        val model = eventService.createModel(digisosSak)

        val soknadVedlegg = soknadVedleggService.hentSoknadVedleggMedStatus(digisosSak, LASTET_OPP_STATUS)
        val ettersendteVedlegg = hentEttersendteVedlegg(digisosSak, model)

        val utestaendeOppgaver = hentUtestaendeOppgaverSomManglendeVedlegg(model, ettersendteVedlegg)

        return soknadVedlegg.plus(ettersendteVedlegg).plus(utestaendeOppgaver)
    }

    fun hentEttersendteVedlegg(digisosSak: DigisosSak, model: InternalDigisosSoker): List<InternalVedlegg> {
        val requestAttributes = getRequestAttributes()

        val alleVedlegg = runBlocking(Dispatchers.IO + MDCContext()) {
            digisosSak.ettersendtInfoNAV?.ettersendelser
                ?.flatMapParallel { ettersendelse ->
                    setRequestAttributes(requestAttributes)
                    val jsonVedleggSpesifikasjon = hentVedleggSpesifikasjon(digisosSak.sokerFnr, digisosSak.fiksDigisosId, ettersendelse.vedleggMetadata)
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
                }
        } ?: emptyList()

        return kombinerAlleLikeVedlegg(alleVedlegg)
    }

    private fun hentUtestaendeOppgaverSomManglendeVedlegg(model: InternalDigisosSoker, ettersendteVedlegg: List<InternalVedlegg>): List<InternalVedlegg> {
        val alleVedlegg = model.oppgaver
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
        return kombinerAlleLikeVedlegg(alleVedlegg)
    }

    private fun hentVedleggSpesifikasjon(fnr: String, fiksDigisosId: String, dokumentlagerId: String): JsonVedleggSpesifikasjon {
        return fiksClient.hentDokument(fnr, fiksDigisosId, dokumentlagerId, JsonVedleggSpesifikasjon::class.java) as JsonVedleggSpesifikasjon
    }

    private fun hentInnsendelsesfristFraOppgave(model: InternalDigisosSoker, vedlegg: JsonVedlegg): LocalDateTime? {
        return model.oppgaver
            .sortedByDescending { it.innsendelsesfrist }
            .firstOrNull { it.tittel == vedlegg.type && it.tilleggsinfo == vedlegg.tilleggsinfo }
            ?.innsendelsesfrist
    }
}
