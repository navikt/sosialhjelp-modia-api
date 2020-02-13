package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSoknadsStatus
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsStatus
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonSoknadsStatus) {

    status = SoknadsStatus.valueOf(hendelse.status.name)

    val tittel = when (hendelse.status) {
        JsonSoknadsStatus.Status.MOTTATT -> {
            val navEnhetsnavn = soknadsmottaker?.navEnhetsnavn

            if (navEnhetsnavn == null) {
                "Søknaden med vedlegg er mottatt"
            } else {
                "Søknaden med vedlegg er mottatt hos $navEnhetsnavn"
            }
        }
        JsonSoknadsStatus.Status.UNDER_BEHANDLING -> "Søknaden er under behandling"
        JsonSoknadsStatus.Status.FERDIGBEHANDLET -> "Søknaden er ferdig behandlet"
        JsonSoknadsStatus.Status.BEHANDLES_IKKE -> "Søknaden behandles ikke"
        else -> throw RuntimeException("Statustype ${hendelse.status.value()} mangler mapping")
    }

    historikk.add(Hendelse(tittel, hendelse.hendelsestidspunkt.toLocalDateTime()))
}