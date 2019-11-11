package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonVedtakFattet
import no.nav.sbl.sosialhjelpmodiaapi.domain.*
import no.nav.sbl.sosialhjelpmodiaapi.saksstatus.DEFAULT_TITTEL
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonVedtakFattet) {

    val utfallString = hendelse.utfall?.name
    val utfall = if (utfallString == null) null else UtfallVedtak.valueOf(utfallString)

    val vedtakFattet = Vedtak(utfall)

    var sakForReferanse = saker.firstOrNull { it.referanse == hendelse.saksreferanse } ?: saker.firstOrNull { it.referanse == "default" }

    if (sakForReferanse == null) {
        // Opprett ny Sak
        sakForReferanse = Sak(
                hendelse.saksreferanse ?: "default",
                SaksStatus.UNDER_BEHANDLING,
                DEFAULT_TITTEL,
                mutableListOf(),
                mutableListOf(),
                mutableListOf(),
                mutableListOf()
        )
        saker.add(sakForReferanse)
    }
    sakForReferanse.vedtak.add(vedtakFattet)

    val sak = saker.first { it.referanse == hendelse.saksreferanse }
    val beskrivelse = "${sak.tittel ?: DEFAULT_TITTEL} er ferdig behandlet"

    historikk.add(Hendelse(beskrivelse, toLocalDateTime(hendelse.hendelsestidspunkt)))
}