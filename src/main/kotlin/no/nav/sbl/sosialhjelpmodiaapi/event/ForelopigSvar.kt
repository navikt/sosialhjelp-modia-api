package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonForelopigSvar
import no.nav.sbl.sosialhjelpmodiaapi.common.VIS_BREVET
import no.nav.sbl.sosialhjelpmodiaapi.domain.ForelopigSvar
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.event.Titler.FORELOPIG_SVAR
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonForelopigSvar) {

    forelopigSvar = ForelopigSvar(hendelse.hendelsestidspunkt.toLocalDateTime())

    historikk.add(Hendelse(FORELOPIG_SVAR, FORELOPIG_SVAR, hendelse.hendelsestidspunkt.toLocalDateTime(), VIS_BREVET))
}
