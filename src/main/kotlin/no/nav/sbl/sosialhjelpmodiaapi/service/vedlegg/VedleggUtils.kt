package no.nav.sbl.sosialhjelpmodiaapi.service.vedlegg

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sosialhjelp.api.fiks.DokumentInfo
import java.time.LocalDateTime

internal fun matchDokumentInfoOgJsonFiler(dokumentInfoList: List<DokumentInfo>, jsonFiler: List<JsonFiler>): Int {
    return jsonFiler
        .flatMap { fil ->
            dokumentInfoList
                .filter { it.filnavn == fil.filnavn }
        }.count()
}

internal fun kombinerAlleLikeVedlegg(alleVedlegg: List<InternalVedlegg>): List<InternalVedlegg> {
    val kombinertListe = ArrayList<InternalVedlegg>()
    alleVedlegg.forEach {
        val funnet = kombinertListe.firstOrNull { kombinert ->
            (
                areDatesEqual(it.datoLagtTil, kombinert.datoLagtTil) &&
                    kombinert.type == it.type &&
                    kombinert.tilleggsinfo == it.tilleggsinfo &&
                    areDatesEqual(it.innsendelsesfrist, kombinert.innsendelsesfrist)
                )
        }
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
