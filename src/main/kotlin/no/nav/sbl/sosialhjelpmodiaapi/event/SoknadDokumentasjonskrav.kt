package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.Oppgave
import no.nav.sbl.sosialhjelpmodiaapi.service.vedlegg.SoknadVedleggService
import no.nav.sbl.sosialhjelpmodiaapi.service.vedlegg.VEDLEGG_KREVES_STATUS
import no.nav.sbl.sosialhjelpmodiaapi.unixToLocalDateTime
import no.nav.sosialhjelp.api.fiks.DigisosSak

fun InternalDigisosSoker.applySoknadKrav(
    digisosSak: DigisosSak,
    soknadVedleggService: SoknadVedleggService,
    timestampSendt: Long
) {
    val vedleggKreves = soknadVedleggService.hentSoknadVedleggMedStatus(digisosSak, VEDLEGG_KREVES_STATUS)

    oppgaver = vedleggKreves
        .filterNot { it.type == "annet" && it.tilleggsinfo == "annet" }
        .map { Oppgave(it.type, it.tilleggsinfo, null, unixToLocalDateTime(timestampSendt), false) }
        .toMutableList()
}
