package no.nav.sosialhjelp.modia.digisossak.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonVedtakFattet
import no.nav.sosialhjelp.modia.digisossak.domain.Hendelse
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.Sak
import no.nav.sosialhjelp.modia.digisossak.domain.SaksStatus
import no.nav.sosialhjelp.modia.digisossak.domain.UtfallVedtak
import no.nav.sosialhjelp.modia.digisossak.domain.Vedtak
import no.nav.sosialhjelp.modia.digisossak.event.Titler.SAK_FERDIGBEHANDLET
import no.nav.sosialhjelp.modia.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonVedtakFattet) {
    val utfallString = hendelse.utfall?.name
    val utfall = if (utfallString == null) null else UtfallVedtak.valueOf(utfallString)

    val vedtak = Vedtak(utfall, hendelse.hendelsestidspunkt.toLocalDateTime().toLocalDate())

    var sakForReferanse = saker.firstOrNull { it.referanse == hendelse.saksreferanse || it.referanse == "default" }

    if (sakForReferanse == null) {
        // Opprett ny Sak
        sakForReferanse = Sak(
            hendelse.saksreferanse ?: "default",
            SaksStatus.UNDER_BEHANDLING,
            SAK_DEFAULT_TITTEL,
            mutableListOf(),
            mutableListOf(),
            hendelse.hendelsestidspunkt.toLocalDateTime().toLocalDate()
        )
        saker.add(sakForReferanse)
    }
    sakForReferanse.vedtak.add(vedtak)

    val beskrivelse = "${sakForReferanse.tittel ?: SAK_DEFAULT_TITTEL} er ferdigbehandlet"

    historikk.add(Hendelse(SAK_FERDIGBEHANDLET, beskrivelse, hendelse.hendelsestidspunkt.toLocalDateTime(), VIS_BREVET))
}
