package no.nav.sosialhjelp.modia.digisossak.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonForelopigSvar
import no.nav.sosialhjelp.modia.digisossak.domain.ForelopigSvar
import no.nav.sosialhjelp.modia.digisossak.domain.Hendelse
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.event.Titler.FORELOPIG_SVAR
import no.nav.sosialhjelp.modia.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonForelopigSvar) {

    forelopigSvar = ForelopigSvar(hendelse.hendelsestidspunkt.toLocalDateTime())

    historikk.add(Hendelse(FORELOPIG_SVAR, FORELOPIG_SVAR, hendelse.hendelsestidspunkt.toLocalDateTime(), VIS_BREVET))
}
