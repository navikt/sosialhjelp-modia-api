package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonForelopigSvar
import no.nav.sbl.sosialhjelpmodiaapi.domain.ForelopigSvar
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonForelopigSvar) {

    forelopigSvar = ForelopigSvar(hendelse.hendelsestidspunkt.toLocalDateTime())

    val beskrivelse = "Du har fått et brev om saksbehandlingstiden for søknaden din"

    historikk.add(Hendelse(beskrivelse, hendelse.hendelsestidspunkt.toLocalDateTime()))
}