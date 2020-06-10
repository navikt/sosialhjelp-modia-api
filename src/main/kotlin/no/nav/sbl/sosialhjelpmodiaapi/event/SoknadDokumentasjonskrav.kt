package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.Oppgave
import no.nav.sbl.sosialhjelpmodiaapi.service.vedlegg.VEDLEGG_KREVES_STATUS
import no.nav.sbl.sosialhjelpmodiaapi.service.vedlegg.VedleggService
import no.nav.sbl.sosialhjelpmodiaapi.unixToLocalDateTime
import no.nav.sosialhjelp.api.fiks.OriginalSoknadNAV

fun InternalDigisosSoker.applySoknadKrav(
        fiksDigisosId: String,
        originalSoknadNAV: OriginalSoknadNAV,
        vedleggService: VedleggService,
        timestampSendt: Long
) {
    val vedleggKreves = vedleggService.hentSoknadVedleggMedStatus(VEDLEGG_KREVES_STATUS, fiksDigisosId, originalSoknadNAV)

    oppgaver = vedleggKreves
            .filterNot { it.type == "annet" && it.tilleggsinfo == "annet" }
            .map { Oppgave(it.type, it.tilleggsinfo, null, unixToLocalDateTime(timestampSendt), false) }
            .toMutableList()
}