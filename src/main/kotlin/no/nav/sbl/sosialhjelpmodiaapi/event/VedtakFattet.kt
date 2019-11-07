package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonVedtakFattet
import no.nav.sbl.sosialhjelpmodiaapi.domain.*
import no.nav.sbl.sosialhjelpmodiaapi.saksstatus.DEFAULT_TITTEL
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonVedtakFattet) {

    val utfallString = hendelse.utfall?.name
    val utfall = if (utfallString == null) null else UtfallVedtak.valueOf(utfallString)

    val vedtakFattet = Vedtak(utfall)

    val sakForReferanse = saker.firstOrNull { it.referanse == hendelse.saksreferanse }
    if (sakForReferanse != null) {
        sakForReferanse.vedtak.add(vedtakFattet)
    } else {
        val sak = Sak(
                hendelse.saksreferanse,
                null,
                null,
                mutableListOf(vedtakFattet),
                mutableListOf(),
                mutableListOf()
        )
        saker.add(sak)
    }

    val sak = saker.first { it.referanse == hendelse.saksreferanse }
    val beskrivelse = "${sak.tittel ?: DEFAULT_TITTEL} er ferdig behandlet"

    historikk.add(Hendelse(beskrivelse, toLocalDateTime(hendelse.hendelsestidspunkt)))
}