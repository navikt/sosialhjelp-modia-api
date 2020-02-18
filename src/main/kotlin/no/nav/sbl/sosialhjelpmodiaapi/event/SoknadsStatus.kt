package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSoknadsStatus
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsStatus
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonSoknadsStatus) {

    status = SoknadsStatus.valueOf(hendelse.status.name)

    val tittel = when (hendelse.status) {
        JsonSoknadsStatus.Status.MOTTATT -> SOKNAD_MOTTATT
        JsonSoknadsStatus.Status.UNDER_BEHANDLING -> SOKNAD_UNDER_BEHANDLING
        JsonSoknadsStatus.Status.FERDIGBEHANDLET -> SOKNAD_FERDIGBEHANDLET
        JsonSoknadsStatus.Status.BEHANDLES_IKKE -> SOKNAD_BEHANDLES_IKKE
        else -> throw RuntimeException("Statustype ${hendelse.status.value()} mangler mapping")
    }

    val beskrivelse: String? = if (tittel == SOKNAD_MOTTATT) {
        val navEnhetsnavn = soknadsmottaker?.navEnhetsnavn
        if (navEnhetsnavn == null) {
            "Søknaden med vedlegg er mottatt"
        } else {
            "Søknaden med vedlegg er mottatt hos $navEnhetsnavn"
        }
    } else {
        null
    }

    historikk.add(Hendelse(tittel, beskrivelse, hendelse.hendelsestidspunkt.toLocalDateTime()))
}