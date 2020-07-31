package no.nav.sbl.sosialhjelpmodiaapi.service.noekkelinfo

import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.NavKontor
import no.nav.sbl.sosialhjelpmodiaapi.domain.NavKontorInformasjon
import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadNoekkelinfoResponse
import no.nav.sbl.sosialhjelpmodiaapi.domain.VideresendtInfo
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.hentSoknadTittel
import no.nav.sbl.sosialhjelpmodiaapi.unixToLocalDateTime
import org.springframework.stereotype.Component

@Component
class NoekkelinfoService(
        private val fiksClient: FiksClient,
        private val eventService: EventService
) {

    fun hentNoekkelInfo(fiksDigisosId: String): SoknadNoekkelinfoResponse {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)
        val model = eventService.createModel(digisosSak)

        val behandlendeNavKontor: NavKontorInformasjon? = model.navKontorHistorikk.lastOrNull()

        return SoknadNoekkelinfoResponse(
                tittel = hentSoknadTittel(digisosSak, model),
                status = model.status!!,
                sistOppdatert = unixToLocalDateTime(digisosSak.sistEndret).toLocalDate(),
                saksId = null, // TODO: saksreferanse eller behandlingsid?
                sendtEllerMottattTidspunkt = model.historikk[0].tidspunkt.toLocalDate(), // Første hendelse i historikk er alltid SENDT eller MOTTATT (hvis papirsøknad)
                navKontor = behandlendeNavKontor?.let { NavKontor(it.navEnhetsnavn, it.navEnhetsnummer) }, // null hvis papirsøknad og ikke enda mottatt
                videresendtHistorikk = leggTilVideresendtInfoHvisNavKontorHistorikkHarFlereElementer(model),
                tidspunktForelopigSvar = model.forelopigSvar?.hendelseTidspunkt
        )
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