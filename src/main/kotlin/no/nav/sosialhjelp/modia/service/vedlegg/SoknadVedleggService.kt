package no.nav.sosialhjelp.modia.service.vedlegg

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.modia.client.fiks.FiksClient
import no.nav.sosialhjelp.modia.unixToLocalDateTime
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.springframework.stereotype.Component

const val LASTET_OPP_STATUS = "LastetOpp"
const val VEDLEGG_KREVES_STATUS = "VedleggKreves"

@Component
class SoknadVedleggService(
        private val fiksClient: FiksClient,
) {

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
        return kombinerAlleLikeVedlegg(alleVedlegg)
    }

    private fun hentVedleggSpesifikasjon(fnr: String, fiksDigisosId: String, dokumentlagerId: String): JsonVedleggSpesifikasjon {
        return fiksClient.hentDokument(fnr, fiksDigisosId, dokumentlagerId, JsonVedleggSpesifikasjon::class.java) as JsonVedleggSpesifikasjon
    }

}