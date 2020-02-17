package no.nav.sbl.sosialhjelpmodiaapi.noekkelinfo

import no.nav.sbl.sosialhjelpmodiaapi.domain.NavEnhet
import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadNoekkelinfoResponse
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.hentSoknadTittel
import no.nav.sbl.sosialhjelpmodiaapi.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.unixToLocalDateTime
import org.springframework.stereotype.Component

@Component
class NoekkelinfoService(private val fiksClient: FiksClient,
                         private val eventService: EventService,
                         private val norgClient: NorgClient) {

    fun hentNoekkelInfo(fiksDigisosId: String, sporingsId: String): SoknadNoekkelinfoResponse {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId, sporingsId)
        val model = eventService.createModel(digisosSak, sporingsId)

        val navEnhetSoknad: NavEnhet? = model.soknadsmottaker?.navEnhetsnummer?.let { norgClient.hentNavEnhet(it) }
        val navEnhetTildelt: NavEnhet? = model.tildeltNavKontor?.let { norgClient.hentNavEnhet(it) }

        return SoknadNoekkelinfoResponse(
                tittel = hentSoknadTittel(digisosSak, model),
                status = model.status!!,
                sistOppdatert = unixToLocalDateTime(digisosSak.sistEndret),
                saksId = null, // TODO: skal saksreferanse med?
                sendtEllerMottattTidspunkt = model.historikk[0].tidspunkt, // Første hendelse i historikk er alltid SENDT eller MOTTATT (hvis papirsøknad)
                navKontor = navEnhetTildelt?.navn ?: navEnhetSoknad?.navn, // null hvis papirsøknad?
                videresendt = navEnhetTildelt != null,
                tidspunktForelopigSvar = model.forelopigSvar?.hendelseTidspunkt
        )
    }
}