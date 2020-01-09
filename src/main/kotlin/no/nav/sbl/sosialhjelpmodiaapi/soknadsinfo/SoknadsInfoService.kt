package no.nav.sbl.sosialhjelpmodiaapi.soknadsinfo

import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsInfoResponse
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.hentSoknadTittel
import no.nav.sbl.sosialhjelpmodiaapi.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.unixToLocalDateTime
import org.springframework.stereotype.Component

@Component
class SoknadsInfoService(private val fiksClient: FiksClient,
                         private val eventService: EventService,
                         private val norgClient: NorgClient) {

    // TODO: vurdere om SoknadsInfoResponse skal reduseres til å _kun_ inneholde
    //  soknadSendtMottattTidspunkt, navKontorSoknad, navKontorTildelt, tidspunktForelopigSvar og navKontorSaksbehandlingstid
    //  Mulig at følgende fjernes (eller blir eget endepunkt): status, tittel og sistOppdatert
    //  Mulig utvidelse: Oppgaver {antall dokumentkrav, neste frist}, Utbetalinger {harVilkår, antall utførte, antall til forfall, antall stoppet}

    fun hentSoknadsInfo(fiksDigisosId: String, sporingsId: String): SoknadsInfoResponse {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId, sporingsId)
        val model = eventService.createModel(digisosSak, sporingsId)

        val navEnhetSoknad = model.soknadsmottaker?.navEnhetsnummer?.let { norgClient.hentNavEnhet(it) }
        val navEnhetTildelt = model.tildeltNavKontor?.let { norgClient.hentNavEnhet(it) }

        return SoknadsInfoResponse(
                status = model.status!!,
                tittel = hentSoknadTittel(digisosSak, model),
                sistOppdatert = unixToLocalDateTime(digisosSak.sistEndret),
                soknadSendtMottattTidspunkt = model.historikk[0].tidspunkt, // Første hendelse i historikk er alltid SENDT eller MOTTATT (hvis papirsøknad)
                navKontorSoknad = model.soknadsmottaker?.navEnhetsnavn, // null hvis papirsøknad?
                navKontorTildelt = navEnhetTildelt?.navn,
                tidspunktForelopigSvar = model.forelopigSvar?.hendelseTidspunkt,
                navKontorSaksbehandlingstid = navEnhetTildelt?.sosialeTjenester ?: navEnhetSoknad?.sosialeTjenester // info om sosialTjenester for navkontor søknad er videresendt til eller sendt til/mottatt
        )
    }
}