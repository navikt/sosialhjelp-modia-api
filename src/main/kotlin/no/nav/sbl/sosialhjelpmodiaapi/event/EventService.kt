package no.nav.sbl.sosialhjelpmodiaapi.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonHendelse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonDokumentasjonEtterspurt
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonDokumentasjonkrav
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonForelopigSvar
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonRammevedtak
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSaksStatus
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSoknadsStatus
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonTildeltNavKontor
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonUtbetaling
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonVedtakFattet
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonVilkar
import no.nav.sbl.sosialhjelpmodiaapi.client.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.common.VIS_SOKNADEN
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.NavKontorInformasjon
import no.nav.sbl.sosialhjelpmodiaapi.domain.SendingType
import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsStatus
import no.nav.sbl.sosialhjelpmodiaapi.domain.Soknadsmottaker
import no.nav.sbl.sosialhjelpmodiaapi.event.Titler.SOKNAD_SENDT
import no.nav.sbl.sosialhjelpmodiaapi.service.innsyn.InnsynService
import no.nav.sbl.sosialhjelpmodiaapi.unixToLocalDateTime
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.springframework.stereotype.Component

@Component
class EventService(
        private val innsynService: InnsynService,
        private val norgClient: NorgClient
) {

    fun createModel(digisosSak: DigisosSak): InternalDigisosSoker {
        val jsonDigisosSoker: JsonDigisosSoker? = innsynService.hentJsonDigisosSoker(digisosSak.sokerFnr, digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata)
        val timestampSendt = digisosSak.originalSoknadNAV?.timestampSendt

        val enhetsnummer: String? = digisosSak.tilleggsinformasjon?.enhetsnummer

        val model = InternalDigisosSoker()

        if (timestampSendt != null) {
            model.status = SoknadsStatus.SENDT

            if (enhetsnummer != null) {
                val navEnhetsnavn = norgClient.hentNavEnhet(enhetsnummer).navn
                model.soknadsmottaker = Soknadsmottaker(enhetsnummer, navEnhetsnavn)
                model.historikk.add(Hendelse(SOKNAD_SENDT, "Søknaden med vedlegg er sendt til $navEnhetsnavn.", unixToLocalDateTime(timestampSendt), VIS_SOKNADEN))
                model.navKontorHistorikk.add(NavKontorInformasjon(SendingType.SENDT, unixToLocalDateTime(timestampSendt), enhetsnummer, navEnhetsnavn))
            }
        }

        jsonDigisosSoker?.hendelser
                ?.sortedBy { it.hendelsestidspunkt }
                ?.forEach { model.applyHendelse(it) }

        return model
    }

    fun createSoknadsoversiktModel(digisosSak: DigisosSak): InternalDigisosSoker {
        val jsonDigisosSoker: JsonDigisosSoker? = innsynService.hentJsonDigisosSoker(digisosSak.sokerFnr, digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata)
        val timestampSendt = digisosSak.originalSoknadNAV?.timestampSendt

        val model = InternalDigisosSoker()
        if (timestampSendt != null) {
            model.status = SoknadsStatus.SENDT
        }
        if (jsonDigisosSoker == null) {
            return model
        }
        jsonDigisosSoker.hendelser
                .sortedBy { it.hendelsestidspunkt }
                .forEach { model.applyHendelse(it) }

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