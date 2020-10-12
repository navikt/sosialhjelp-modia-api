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
import no.nav.sbl.sosialhjelpmodiaapi.utils.DEFAULT_NAVENHETSNAVN
import no.nav.sbl.sosialhjelpmodiaapi.utils.navenhetsnavnOrDefault
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

        val model = InternalDigisosSoker()
        // Default status == SENDT. Gjelder også for papirsøknader hvor timestampSendt == null
        model.status = SoknadsStatus.SENDT

        if (timestampSendt != null) {
            val enhetsnummer: String = digisosSak.tilleggsinformasjon?.enhetsnummer ?: ""
            val navenhetsnavn = getNavenhetsnavnOrDefault(enhetsnummer)

            model.soknadsmottaker = Soknadsmottaker(enhetsnummer, navenhetsnavn)
            model.historikk.add(Hendelse(SOKNAD_SENDT, "Søknaden med vedlegg er sendt til $navenhetsnavn, {{kommunenavn}}.", unixToLocalDateTime(timestampSendt), VIS_SOKNADEN))
            model.navKontorHistorikk.add(NavKontorInformasjon(SendingType.SENDT, unixToLocalDateTime(timestampSendt), enhetsnummer, navenhetsnavn))
        }

        jsonDigisosSoker?.hendelser
                ?.sortedBy { it.hendelsestidspunkt }
                ?.forEach { model.applyHendelse(it) }

        return model
    }

    fun getNavenhetsnavnOrDefault(enhetsnummer: String): String {
        if (enhetsnummer.isEmpty()) return DEFAULT_NAVENHETSNAVN
        val navn = norgClient.hentNavEnhet(enhetsnummer)?.navn
        return navenhetsnavnOrDefault(navn)
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