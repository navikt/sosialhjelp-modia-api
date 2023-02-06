package no.nav.sosialhjelp.modia.digisossak.event

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
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.modia.digisossak.domain.Hendelse
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.NavKontorInformasjon
import no.nav.sosialhjelp.modia.digisossak.domain.SendingType
import no.nav.sosialhjelp.modia.digisossak.domain.SoknadsStatus
import no.nav.sosialhjelp.modia.digisossak.domain.Soknadsmottaker
import no.nav.sosialhjelp.modia.digisossak.event.Titler.SOKNAD_SENDT
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.navkontor.norg.NorgClient
import no.nav.sosialhjelp.modia.soknad.vedlegg.SoknadVedleggService
import no.nav.sosialhjelp.modia.unixToLocalDateTime
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class EventService(
    private val jsonDigisosSokerService: JsonDigisosSokerService,
    private val norgClient: NorgClient,
    private val soknadVedleggService: SoknadVedleggService
) {

    fun createModel(digisosSak: DigisosSak): InternalDigisosSoker {
        val jsonDigisosSoker: JsonDigisosSoker? = jsonDigisosSokerService.get(digisosSak.sokerFnr, digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata, digisosSak.digisosSoker?.timestampSistOppdatert)
        val timestampSendt = digisosSak.originalSoknadNAV?.timestampSendt

        val model = InternalDigisosSoker()

        if (timestampSendt != null) {
            val enhetsnummer: String = digisosSak.tilleggsinformasjon?.enhetsnummer ?: ""
            val navenhetsnavn = getNavenhetsnavnOrDefault(enhetsnummer)

            model.soknadsmottaker = Soknadsmottaker(enhetsnummer, navenhetsnavn)
            model.historikk.add(Hendelse(SOKNAD_SENDT, "Søknaden med vedlegg er sendt til $navenhetsnavn.", unixToLocalDateTime(timestampSendt), VIS_SOKNADEN))
            model.navKontorHistorikk.add(NavKontorInformasjon(SendingType.SENDT, unixToLocalDateTime(timestampSendt), enhetsnummer, navenhetsnavn))
        }

        jsonDigisosSoker?.hendelser
            ?.sortedWith(hendelseComparator)
            ?.forEach { model.applyHendelse(it) }

        val originalSoknadNAV = digisosSak.originalSoknadNAV
        if (originalSoknadNAV != null && model.oppgaver.isEmpty() && soknadSendtForMindreEnn30DagerSiden(originalSoknadNAV.timestampSendt)) {
            model.applySoknadKrav(digisosSak, soknadVedleggService, timestampSendt!!)
        }

        return model
    }

    private fun getNavenhetsnavnOrDefault(enhetsnummer: String): String {
        if (enhetsnummer.isEmpty()) return "[Kan ikke hente NAV-kontor uten enhetsnummer]"
        return norgClient.hentNavEnhet(enhetsnummer)?.navn?.takeUnless { it.isEmpty() }
            ?: "[Kan ikke hente NAV-kontor for enhetsnummer: \"$enhetsnummer]\""
    }

    fun createSoknadsoversiktModel(digisosSak: DigisosSak): InternalDigisosSoker {
        val jsonDigisosSoker: JsonDigisosSoker? = jsonDigisosSokerService.get(digisosSak.sokerFnr, digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata, digisosSak.digisosSoker?.timestampSistOppdatert)
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

        val originalSoknadNAV = digisosSak.originalSoknadNAV
        if (originalSoknadNAV != null && model.oppgaver.isEmpty() && soknadSendtForMindreEnn30DagerSiden(originalSoknadNAV.timestampSendt)) {
            model.applySoknadKrav(digisosSak, soknadVedleggService, timestampSendt!!)
        }

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

    private fun soknadSendtForMindreEnn30DagerSiden(timestampSendt: Long) =
        unixToLocalDateTime(timestampSendt).toLocalDate().isAfter(LocalDate.now().minusDays(30))

    companion object {
        private val log by logger()

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
