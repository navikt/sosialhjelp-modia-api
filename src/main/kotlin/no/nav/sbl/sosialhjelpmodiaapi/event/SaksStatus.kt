package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSaksStatus
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.Sak
import no.nav.sbl.sosialhjelpmodiaapi.domain.SaksStatus

fun InternalDigisosSoker.apply(hendelse: JsonSaksStatus) {

    val sakForReferanse = saker.firstOrNull { it.referanse == hendelse.referanse }

    if (sakForReferanse != null) {
        // Oppdater felter

        if (hendelse.status != null) {
            sakForReferanse.saksStatus = SaksStatus.valueOf(hendelse.status?.name
                    ?: JsonSaksStatus.Status.UNDER_BEHANDLING.name)
        }

        sakForReferanse.tittel = hendelse.tittel
    } else {
        // Opprett ny Sak
        saker.add(Sak(
                hendelse.referanse,
                SaksStatus.valueOf(hendelse.status?.name ?: JsonSaksStatus.Status.UNDER_BEHANDLING.name),
                hendelse.tittel,
                mutableListOf(),
                mutableListOf(),
                mutableListOf()
        ))
    }
}
