package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonDokumentasjonEtterspurt
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.Oppgave
import no.nav.sbl.sosialhjelpmodiaapi.event.Titler.DOKUMENTASJONSKRAV
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonDokumentasjonEtterspurt) {
    oppgaver = hendelse.dokumenter
            .map { Oppgave(it.dokumenttype, it.tilleggsinformasjon, it.innsendelsesfrist.toLocalDateTime(), hendelse.hendelsestidspunkt.toLocalDateTime(), true) }
            .toMutableList()

    if (hendelse.dokumenter.isNotEmpty() && hendelse.forvaltningsbrev != null) {
        val beskrivelse = "Veileder ber søker sende dokumentasjon. Med forvaltningsbrev."
        historikk.add(Hendelse(DOKUMENTASJONSKRAV, beskrivelse, hendelse.hendelsestidspunkt.toLocalDateTime()))
    }
}