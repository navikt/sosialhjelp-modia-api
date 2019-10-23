package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonDokumentasjonEtterspurt
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.Oppgave
import no.nav.sbl.sosialhjelpmodiaapi.hentUrlFraFilreferanse
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime

fun InternalDigisosSoker.apply(hendelse: JsonDokumentasjonEtterspurt, clientProperties: ClientProperties) {
    oppgaver = hendelse.dokumenter
            .map { Oppgave(it.dokumenttype, it.tilleggsinformasjon, toLocalDateTime(it.innsendelsesfrist), toLocalDateTime(hendelse.hendelsestidspunkt), true) }
            .toMutableList()

    val antallKrav = if (hendelse.dokumenter.isEmpty()) "Ingen" else hendelse.dokumenter.size.toString()
    val beskrivelse = "Veileder har oppdatert dine dokumentasjonskrav: $antallKrav vedlegg mangler"
    val url = if (hendelse.dokumenter.isEmpty()) null else hentUrlFraFilreferanse(clientProperties, hendelse.forvaltningsbrev.referanse)
    historikk.add(Hendelse(beskrivelse, toLocalDateTime(hendelse.hendelsestidspunkt), url))
}