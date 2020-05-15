package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonVedtakFattet
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonVedtaksfil
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

    val beskrivelse = beskrivelse(sakForReferanse, hendelse.vedtaksfil)

    historikk.add(Hendelse(SAK_FERDIGBEHANDLET, beskrivelse, hendelse.hendelsestidspunkt.toLocalDateTime()))
}

private fun beskrivelse(sak: Sak, vedtaksfil: JsonVedtaksfil?): String {
    val beskrivelse = "${sak.tittel ?: DEFAULT_TITTEL} er ferdig behandlet."
    val vedtaksbrev = if (vedtaksfil == null || vedtaksfil.referanse == null) null else "Med vedtaksbrev."
    return "$beskrivelse $vedtaksbrev"
}