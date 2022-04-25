package no.nav.sosialhjelp.modia.digisossak.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSaksStatus
import no.nav.sosialhjelp.modia.digisossak.domain.Hendelse
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.Sak
import no.nav.sosialhjelp.modia.digisossak.domain.SaksStatus
import no.nav.sosialhjelp.modia.toLocalDateTime
import java.util.Locale

fun InternalDigisosSoker.apply(hendelse: JsonSaksStatus) {

    val sakForReferanse = saker.firstOrNull { it.referanse == hendelse.referanse }

    if (sakForReferanse != null) {
        // Oppdater felter
        sakForReferanse.tittel = hendelse.tittel

        if (hendelse.status != null) {
            val prevStatus = sakForReferanse.saksStatus

            sakForReferanse.saksStatus = SaksStatus.valueOf(
                hendelse.status?.name
                    ?: JsonSaksStatus.Status.UNDER_BEHANDLING.name
            )

            if (prevStatus != sakForReferanse.saksStatus &&
                (sakForReferanse.saksStatus == SaksStatus.IKKE_INNSYN || sakForReferanse.saksStatus == SaksStatus.BEHANDLES_IKKE)
            ) {
                val tittel = sakForReferanse.tittel ?: "saken din"
                historikk.add(
                    Hendelse(
                        tittel,
                        "Vi kan ikke vise status for søknaden din $tittel på nav.no",
                        hendelse.hendelsestidspunkt.toLocalDateTime()
                    )
                )
            }
        }
    } else {
        // Opprett ny Sak
        val status = SaksStatus.valueOf(hendelse.status?.name ?: JsonSaksStatus.Status.UNDER_BEHANDLING.name)
        saker.add(
            Sak(
                referanse = hendelse.referanse,
                saksStatus = status,
                tittel = hendelse.tittel,
                vedtak = mutableListOf(),
                utbetalinger = mutableListOf(),
                datoOpprettet = hendelse.hendelsestidspunkt.toLocalDateTime().toLocalDate()
            )
        )
        val tittel = hendelse.tittel ?: "saken din"
        val beskrivelse: String? = when (status) {
            SaksStatus.UNDER_BEHANDLING -> "${tittel.replaceFirstChar { it.titlecase(Locale.getDefault()) }} er under behandling"
            SaksStatus.BEHANDLES_IKKE, SaksStatus.IKKE_INNSYN -> "Vi kan ikke vise status for søknaden din $tittel på nav.no"
            else -> null
        }
        if (beskrivelse != null) {
            historikk.add(Hendelse(tittel, beskrivelse, hendelse.hendelsestidspunkt.toLocalDateTime()))
        }
    }
}
