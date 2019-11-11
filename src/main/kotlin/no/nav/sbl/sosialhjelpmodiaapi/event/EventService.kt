package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonHendelse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.*
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.sosialhjelpmodiaapi.domain.*
import no.nav.sbl.sosialhjelpmodiaapi.innsyn.InnsynService
import no.nav.sbl.sosialhjelpmodiaapi.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.unixToLocalDateTime
import org.springframework.stereotype.Component

@Component
class EventService(private val innsynService: InnsynService,
                   private val norgClient: NorgClient) {

    fun createModel(digisosSak: DigisosSak, token: String): InternalDigisosSoker {
        val jsonDigisosSoker: JsonDigisosSoker? = innsynService.hentJsonDigisosSoker(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata, token)
        val jsonSoknad: JsonSoknad? = innsynService.hentOriginalSoknad(digisosSak.fiksDigisosId, digisosSak.originalSoknadNAV?.metadata, token)
        val timestampSendt = digisosSak.originalSoknadNAV?.timestampSendt

        val model = InternalDigisosSoker()

        if (jsonSoknad != null && jsonSoknad.mottaker != null && timestampSendt != null) {
            model.soknadsmottaker = Soknadsmottaker(jsonSoknad.mottaker.enhetsnummer, jsonSoknad.mottaker.navEnhetsnavn)
            model.historikk.add(Hendelse("Søknaden med vedlegg er sendt til ${jsonSoknad.mottaker.navEnhetsnavn}", unixToLocalDateTime(timestampSendt)))
        }

        if (jsonDigisosSoker != null) {
            jsonDigisosSoker.hendelser
                    .sortedBy { it.hendelsestidspunkt }
                    .forEach { model.applyHendelse(it) }
        }

        return model
    }

    fun InternalDigisosSoker.applyHendelse(hendelse: JsonHendelse) {
        when (hendelse) {
            is JsonSoknadsStatus -> apply(hendelse)
            is JsonTildeltNavKontor -> apply(hendelse, norgClient)
            is JsonSaksStatus -> apply(hendelse)
            is JsonVedtakFattet -> apply(hendelse)
            is JsonDokumentasjonEtterspurt -> apply(hendelse)
            is JsonForelopigSvar -> apply(hendelse)
            is JsonUtbetaling -> apply(hendelse)
            is JsonVilkar -> apply(hendelse)
            is JsonDokumentasjonkrav -> apply(hendelse)
            is JsonRammevedtak -> apply(hendelse) // Gjør ingenting as of now
            else -> throw RuntimeException("Hendelsetype ${hendelse.type.value()} mangler mapping")
        }
    }
}