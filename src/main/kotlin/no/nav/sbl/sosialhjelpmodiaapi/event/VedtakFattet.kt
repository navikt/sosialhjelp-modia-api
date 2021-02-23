package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonVedtakFattet
import no.nav.sbl.sosialhjelpmodiaapi.common.VIS_BREVET
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.Sak
import no.nav.sbl.sosialhjelpmodiaapi.domain.SaksStatus
import no.nav.sbl.sosialhjelpmodiaapi.domain.UtfallVedtak
import no.nav.sbl.sosialhjelpmodiaapi.domain.Vedtak
import no.nav.sbl.sosialhjelpmodiaapi.event.Titler.SAK_FERDIGBEHANDLET
import no.nav.sbl.sosialhjelpmodiaapi.service.saksstatus.DEFAULT_TITTEL
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime

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
            DEFAULT_TITTEL,
            mutableListOf(),
            mutableListOf(),
            hendelse.hendelsestidspunkt.toLocalDateTime().toLocalDate()
        )
        saker.add(sakForReferanse)
    }
    sakForReferanse.vedtak.add(vedtak)

    val beskrivelse = "${sakForReferanse.tittel ?: DEFAULT_TITTEL} er ferdig behandlet"

    historikk.add(Hendelse(SAK_FERDIGBEHANDLET, beskrivelse, hendelse.hendelsestidspunkt.toLocalDateTime(), VIS_BREVET))
}
