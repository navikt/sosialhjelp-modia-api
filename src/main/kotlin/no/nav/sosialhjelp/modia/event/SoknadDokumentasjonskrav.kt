package no.nav.sosialhjelp.modia.event

import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.modia.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.domain.Oppgave
import no.nav.sosialhjelp.modia.soknad.vedlegg.SoknadVedleggService
import no.nav.sosialhjelp.modia.soknad.vedlegg.VEDLEGG_KREVES_STATUS
import no.nav.sosialhjelp.modia.unixToLocalDateTime

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
