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

    fun hentSoknadsInfo(fiksDigisosId: String, sporingsId: String): SoknadsInfoResponse {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId, sporingsId)
        val model = eventService.createModel(digisosSak, sporingsId)

        val enhetsnr: String? = model.tildeltNavKontor ?: model.soknadsmottaker?.navEnhetsnummer
        val navEnhet = enhetsnr?.let { norgClient.hentNavEnhet(it) }

        return SoknadsInfoResponse(
                status = model.status!!,
                tittel = hentSoknadTittel(digisosSak, model),
                sistOppdatert = unixToLocalDateTime(digisosSak.sistEndret),
                sendtTidspunkt = model.historikk[0].tidspunkt, // Første hendelse i historikk er alltid SENDT eller MOTTATT (hvis papirsøknad)
                navKontorSendtTil = model.soknadsmottaker?.navEnhetsnavn ?: "NAVKONTOR", // hva hvis papirsøknad og ikke videresendt?
                navKontorVideresendtTil = navEnhet?.navn,
                tidspunktForelopigSvar = model.forelopigSvar?.hendelseTidspunkt,
                navKontorSaksbehandlingstid = navEnhet?.sosialeTjenester // info om sosialTjenester for navkontor søknad er videresendt til eller sendt til/mottatt
        )
    }
}