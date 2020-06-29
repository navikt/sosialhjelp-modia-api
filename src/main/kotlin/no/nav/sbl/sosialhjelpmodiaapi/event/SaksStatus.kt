package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSaksStatus
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.Sak
import no.nav.sbl.sosialhjelpmodiaapi.domain.SaksStatus
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonSaksStatus) {

    val sakForReferanse = saker.firstOrNull { it.referanse == hendelse.referanse }

    if (sakForReferanse != null) {
        // Oppdater felter

        if (hendelse.status != null) {
            sakForReferanse.saksStatus = SaksStatus.valueOf(hendelse.status?.name
                    ?: JsonSaksStatus.Status.UNDER_BEHANDLING.name)
        }

        sakForReferanse.tittel = hendelse.tittel
        val tittel = sakForReferanse.tittel ?: "saken din"
        historikk.add(Hendelse(tittel,
                "Vi kan ikke vise behandlingsstatus for $tittel digitalt.",
                hendelse.hendelsestidspunkt.toLocalDateTime()))
    } else {
        // Opprett ny Sak
        val status = SaksStatus.valueOf(hendelse.status?.name ?: JsonSaksStatus.Status.UNDER_BEHANDLING.name)
        saker.add(Sak(
                referanse = hendelse.referanse,
                saksStatus = status,
                tittel = hendelse.tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(),
                datoOpprettet = hendelse.hendelsestidspunkt.toLocalDateTime().toLocalDate()
        ))
        val tittel = hendelse.tittel ?: "saken din"
        val beskrivelse: String? = when (status) {
            SaksStatus.UNDER_BEHANDLING -> "${tittel.capitalize()} er under behandling"
            SaksStatus.BEHANDLES_IKKE, SaksStatus.IKKE_INNSYN -> "Vi kan ikke vise behandlingsstatus for $tittel digitalt."
            else -> null
        }
        if (beskrivelse != null) {
            historikk.add(Hendelse(tittel, beskrivelse, hendelse.hendelsestidspunkt.toLocalDateTime()))
        }
    }
}