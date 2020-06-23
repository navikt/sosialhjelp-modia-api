package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonDokumentasjonEtterspurt
import no.nav.sbl.sosialhjelpmodiaapi.common.VIS_BREVET
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.Oppgave
import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsStatus
import no.nav.sbl.sosialhjelpmodiaapi.event.Titler.DOKUMENTASJONSKRAV
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonDokumentasjonEtterspurt) {
    val prevSize = oppgaver.size

    oppgaver = hendelse.dokumenter
            .map { Oppgave(it.dokumenttype, it.tilleggsinformasjon, it.innsendelsesfrist.toLocalDateTime(), hendelse.hendelsestidspunkt.toLocalDateTime(), true) }
            .toMutableList()

    if (hendelse.dokumenter.isNotEmpty() && hendelse.forvaltningsbrev != null) {
        val beskrivelse = "Veileder ber søker sende dokumentasjon. Med forvaltningsbrev."
        historikk.add(Hendelse(DOKUMENTASJONSKRAV, beskrivelse, hendelse.hendelsestidspunkt.toLocalDateTime(), VIS_BREVET))
    }

    if (prevSize > 0 && oppgaver.size == 0 && status != SoknadsStatus.FERDIGBEHANDLET && status != SoknadsStatus.BEHANDLES_IKKE) {
        val beskrivelse = "Vi har sett på dokumentene dine og vil gi beskjed om vi trenger mer fra deg."
        historikk.add(Hendelse(DOKUMENTASJONSKRAV, beskrivelse, hendelse.hendelsestidspunkt.toLocalDateTime()))
    }
}