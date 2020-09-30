package no.nav.sbl.sosialhjelpmodiaapi.service.vedlegg

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.flatMapParallel
import no.nav.sbl.sosialhjelpmodiaapi.unixToLocalDateTime
import no.nav.sbl.sosialhjelpmodiaapi.utils.coroutines.RequestContextService
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.DokumentInfo
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import java.time.LocalDateTime

const val LASTET_OPP_STATUS = "LastetOpp"
const val VEDLEGG_KREVES_STATUS = "VedleggKreves"

@Component
class VedleggService(
        private val fiksClient: FiksClient,
        private val eventService: EventService,
        private val requestContextService: RequestContextService
) {

    fun hentAlleOpplastedeVedlegg(fiksDigisosId: String): List<InternalVedlegg> {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)
        val model = eventService.createModel(digisosSak)

        val soknadVedlegg = hentSoknadVedleggMedStatus(digisosSak, LASTET_OPP_STATUS)
        val ettersendteVedlegg = hentEttersendteVedlegg(digisosSak, model)

        val utestaendeOppgaver = hentUtestaendeOppgaverSomManglendeVedlegg(model, ettersendteVedlegg)

        return soknadVedlegg.plus(ettersendteVedlegg).plus(utestaendeOppgaver)
    }

    fun hentSoknadVedleggMedStatus(digisosSak: DigisosSak, status: String): List<InternalVedlegg> {
        val originalSoknadNAV = digisosSak.originalSoknadNAV ?: return emptyList()

        val jsonVedleggSpesifikasjon = hentVedleggSpesifikasjon(digisosSak.sokerFnr, digisosSak.fiksDigisosId, originalSoknadNAV.vedleggMetadata)

        if (jsonVedleggSpesifikasjon.vedlegg.isEmpty()) {
            return emptyList()
        }

        val alleVedlegg = jsonVedleggSpesifikasjon.vedlegg
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
        return kombinerAlleLikeVedlgg(alleVedlegg)
    }

    fun hentEttersendteVedlegg(digisosSak: DigisosSak, model: InternalDigisosSoker): List<InternalVedlegg> {
        val alleVedlegg = runBlocking(
                context = requestContextService.getCoroutineContext(
                        context = GlobalScope.coroutineContext + Dispatchers.IO,
                        requestAttributes = RequestContextHolder.getRequestAttributes()
                )
        ) {
            digisosSak.ettersendtInfoNAV?.ettersendelser
                    ?.flatMapParallel { ettersendelse ->
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

        return kombinerAlleLikeVedlgg(alleVedlegg)
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
        return kombinerAlleLikeVedlgg(alleVedlegg)
    }

    private fun hentVedleggSpesifikasjon(fnr: String, fiksDigisosId: String, dokumentlagerId: String): JsonVedleggSpesifikasjon {
        return fiksClient.hentDokument(fnr, fiksDigisosId, dokumentlagerId, JsonVedleggSpesifikasjon::class.java) as JsonVedleggSpesifikasjon
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

    private fun kombinerAlleLikeVedlgg(alleVedlegg: List<InternalVedlegg>): List<InternalVedlegg> {
        var kombinertListe = ArrayList<InternalVedlegg>()
        alleVedlegg.forEach {
            val funnet = kombinertListe.filter { kombinert ->
                (areDatesEqual(it.datoLagtTil, kombinert.datoLagtTil) &&
                        kombinert.type == it.type &&
                        kombinert.tilleggsinfo == it.tilleggsinfo &&
                        areDatesEqual(it.innsendelsesfrist, kombinert.innsendelsesfrist))
            }.firstOrNull()
            if (funnet != null) {
                funnet.antallFiler += it.antallFiler
            } else {
                kombinertListe.add(it)
            }
        }
        return kombinertListe
    }

    private fun areDatesEqual(firstDate: LocalDateTime?, secondDate: LocalDateTime?): Boolean {
        return (firstDate == null && secondDate == null) ||
                firstDate?.isEqual(secondDate) ?: false
    }

    data class InternalVedlegg(
            val type: String,
            val tilleggsinfo: String?,
            val innsendelsesfrist: LocalDateTime?,
            var antallFiler: Int,
            val datoLagtTil: LocalDateTime?
    )
}
