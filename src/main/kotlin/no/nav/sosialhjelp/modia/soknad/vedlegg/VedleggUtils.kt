package no.nav.sosialhjelp.modia.soknad.vedlegg

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sosialhjelp.api.fiks.DokumentInfo
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

internal fun matchDokumentInfoOgJsonFiler(
    dokumentInfoList: List<DokumentInfo>,
    jsonFiler: List<JsonFiler>,
): Int =
    jsonFiler
        .flatMap { fil ->
            dokumentInfoList
                .filter { it.filnavn == fil.filnavn }
        }.count()

internal fun kombinerAlleLikeVedlegg(alleVedlegg: List<InternalVedlegg>): List<InternalVedlegg> {
    val kombinertListe = ArrayList<InternalVedlegg>()
    alleVedlegg.forEach {
        val funnet =
            kombinertListe.firstOrNull { kombinert ->
                (
                    areDatesWithinOneMinute(it.datoLagtTil, kombinert.datoLagtTil) &&
                        kombinert.type == it.type &&
                        kombinert.tilleggsinfo == it.tilleggsinfo &&
                        areDatesWithinOneMinute(it.innsendelsesfrist, kombinert.innsendelsesfrist)
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

private fun areDatesWithinOneMinute(
    firstDate: LocalDateTime?,
    secondDate: LocalDateTime?,
): Boolean =
    (firstDate == null && secondDate == null) ||
        ChronoUnit.MINUTES.between(firstDate, secondDate).absoluteValue < 1

data class InternalVedlegg(
    val type: String,
    val tilleggsinfo: String?,
    val innsendelsesfrist: LocalDateTime?,
    var antallFiler: Int,
    val datoLagtTil: LocalDateTime?,
    val tidspunktLastetOpp: LocalDateTime?,
    val tittelForVeileder: String? = null,
    val beskrivelseForVeileder: String? = null,
)
