package no.nav.sosialhjelp.modia.soknad.noekkelinfo

import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.NavKontorInformasjon
import no.nav.sosialhjelp.modia.digisossak.event.EventService
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksClient
import no.nav.sosialhjelp.modia.hentSoknadTittel
import no.nav.sosialhjelp.modia.kommune.KommuneService
import no.nav.sosialhjelp.modia.kommune.KommunenavnService
import no.nav.sosialhjelp.modia.unixToLocalDateTime
import org.springframework.stereotype.Component

@Component
class NoekkelinfoService(
    private val fiksClient: FiksClient,
    private val eventService: EventService,
    private val kommunenavnService: KommunenavnService,
    private val kommuneService: KommuneService
) {

    fun hentNoekkelInfo(fiksDigisosId: String): SoknadNoekkelinfoResponse {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)
        val model = eventService.createModel(digisosSak)
        val kommunenavn = hentBehandlendekommune(digisosSak.kommunenummer)

        val behandlendeNavKontor: NavKontorInformasjon? = model.navKontorHistorikk.lastOrNull()

        return SoknadNoekkelinfoResponse(
            tittel = hentSoknadTittel(digisosSak, model),
            status = model.status,
            sistOppdatert = unixToLocalDateTime(digisosSak.sistEndret).toLocalDate(),
            sendtEllerMottattTidspunkt = model.historikk[0].tidspunkt.toLocalDate(), // Første hendelse i historikk er alltid SENDT eller MOTTATT (hvis papirsøknad)
            navKontor = behandlendeNavKontor?.let { NavKontor(it.navEnhetsnavn, it.navEnhetsnummer) }, // null hvis papirsøknad og ikke enda mottatt
            kommunenavn = kommunenavn,
            videresendtHistorikk = leggTilVideresendtInfoHvisNavKontorHistorikkHarFlereElementer(model),
            tidspunktForelopigSvar = model.forelopigSvar?.hendelseTidspunkt
        )
    }

    private fun hentBehandlendekommune(kommunenummer: String): String {
        return kommuneService.getBehandlingsanvarligKommune(kommunenummer) ?: kommunenavnService.hentKommunenavnFor(kommunenummer)
    }

    private fun leggTilVideresendtInfoHvisNavKontorHistorikkHarFlereElementer(model: InternalDigisosSoker): List<VideresendtInfo>? {
        return if (model.navKontorHistorikk.size > 1)
            model.navKontorHistorikk
                .map {
                    VideresendtInfo(
                        type = it.type,
                        tidspunkt = it.tidspunkt.toLocalDate(),
                        navKontor = NavKontor(it.navEnhetsnavn, it.navEnhetsnummer)
                    )
                }
        else null
    }
}
