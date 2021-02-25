package no.nav.sosialhjelp.modia.event

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
import no.nav.sosialhjelp.modia.client.norg.NorgClient
import no.nav.sosialhjelp.modia.common.VIS_SOKNADEN
import no.nav.sosialhjelp.modia.domain.Hendelse
import no.nav.sosialhjelp.modia.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.domain.NavKontorInformasjon
import no.nav.sosialhjelp.modia.domain.SendingType
import no.nav.sosialhjelp.modia.domain.SoknadsStatus
import no.nav.sosialhjelp.modia.domain.Soknadsmottaker
import no.nav.sosialhjelp.modia.event.Titler.SOKNAD_SENDT
import no.nav.sosialhjelp.modia.service.innsyn.InnsynService
import no.nav.sosialhjelp.modia.service.vedlegg.SoknadVedleggService
import no.nav.sosialhjelp.modia.unixToLocalDateTime
import no.nav.sosialhjelp.modia.utils.DEFAULT_NAVENHETSNAVN
import no.nav.sosialhjelp.modia.utils.navenhetsnavnOrDefault
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.springframework.stereotype.Component

@Component
class EventService(
        private val innsynService: InnsynService,
        private val norgClient: NorgClient,
        private val soknadVedleggService: SoknadVedleggService
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
                ?.sortedWith(hendelseComparator)
                ?.forEach { model.applyHendelse(it) }

        if (digisosSak.originalSoknadNAV != null && model.oppgaver.isEmpty()) {
            model.applySoknadKrav(digisosSak, soknadVedleggService, timestampSendt!!)
        }

        return model
    }

    private fun getNavenhetsnavnOrDefault(enhetsnummer: String): String {
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
                .sortedWith(hendelseComparator)
                .forEach { model.applyHendelse(it) }

        return model
    }

    private fun InternalDigisosSoker.applyHendelse(hendelse: JsonHendelse) {
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

    companion object {

        /**
         * Sorter hendelser på hendelsestidspunkt.
         * Hvis to hendelser har identisk hendelsestidspunkt, og én er Utbetaling og den andre er Vilkår eller Dokumentasjonkrav  -> sorter Utbetaling før Vilkår/Dokumentasjonkrav.
         * Dette gjør at vi kan knytte Vilkår/Dokumentasjonkrav til Utbetalingen.
         */
        private val hendelseComparator = compareBy<JsonHendelse> { it.hendelsestidspunkt }
                .thenComparator { a, b -> compareHendelseByType(a.type, b.type) }

        private fun compareHendelseByType(a: JsonHendelse.Type, b: JsonHendelse.Type): Int {
            if (a == JsonHendelse.Type.UTBETALING && (b == JsonHendelse.Type.VILKAR || b == JsonHendelse.Type.DOKUMENTASJONKRAV)) {
                return -1
            } else if (b == JsonHendelse.Type.UTBETALING && (a == JsonHendelse.Type.VILKAR || a == JsonHendelse.Type.DOKUMENTASJONKRAV)) {
                return 1
            }
            return 0
        }

    }
}