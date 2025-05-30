package no.nav.sosialhjelp.modia.digisossak.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonDokumentasjonEtterspurt
import no.nav.sosialhjelp.modia.digisossak.domain.Hendelse
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.Oppgave
import no.nav.sosialhjelp.modia.digisossak.domain.SoknadsStatus
import no.nav.sosialhjelp.modia.digisossak.event.Titler.DOKUMENTASJONSKRAV
import no.nav.sosialhjelp.modia.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonDokumentasjonEtterspurt) {
    val prevSize = oppgaver.size

    oppgaver =
        hendelse.dokumenter
            .map {
                Oppgave(
                    it.dokumenttype,
                    it.tilleggsinformasjon,
                    it.innsendelsesfrist.toLocalDateTime(),
                    hendelse.hendelsestidspunkt.toLocalDateTime(),
                    true,
                )
            }.toMutableList()

    if (hendelse.dokumenter.isNotEmpty() && hendelse.forvaltningsbrev != null) {
        val beskrivelse = "Vi trenger flere opplysninger til søknaden din"
        historikk.add(Hendelse(DOKUMENTASJONSKRAV, beskrivelse, hendelse.hendelsestidspunkt.toLocalDateTime(), VIS_BREVET))
    }

    if (prevSize > 0 && oppgaver.size == 0 && status != SoknadsStatus.FERDIGBEHANDLET && status != SoknadsStatus.BEHANDLES_IKKE) {
        val beskrivelse = "Vi har sett på opplysningene dine og vil gi beskjed om vi trenger noe mer fra deg"
        historikk.add(Hendelse(DOKUMENTASJONSKRAV, beskrivelse, hendelse.hendelsestidspunkt.toLocalDateTime()))
    }
}
