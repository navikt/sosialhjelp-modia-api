package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.Oppgave
import no.nav.sbl.sosialhjelpmodiaapi.domain.OriginalSoknadNAV
import no.nav.sbl.sosialhjelpmodiaapi.unixToLocalDateTime
import no.nav.sbl.sosialhjelpmodiaapi.vedlegg.VEDLEGG_KREVES_STATUS
import no.nav.sbl.sosialhjelpmodiaapi.vedlegg.VedleggService

fun InternalDigisosSoker.applySoknadKrav(
        fiksDigisosId: String,
        originalSoknadNAV: OriginalSoknadNAV,
        vedleggService: VedleggService,
        timestampSendt: Long,
        token: String
) {
    val vedleggKreves = vedleggService.hentSoknadVedleggMedStatus(VEDLEGG_KREVES_STATUS, fiksDigisosId, originalSoknadNAV, token)

    oppgaver = vedleggKreves
            .filterNot { it.type == "annet" && it.tilleggsinfo == "annet" }
            .map { Oppgave(it.type, it.tilleggsinfo, null, unixToLocalDateTime(timestampSendt), false) }
            .toMutableList()
}