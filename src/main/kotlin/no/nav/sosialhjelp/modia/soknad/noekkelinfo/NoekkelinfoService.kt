package no.nav.sosialhjelp.modia.soknad.noekkelinfo

import no.nav.sosialhjelp.api.fiks.OriginalSoknadNAV
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.NavKontorInformasjon
import no.nav.sosialhjelp.modia.digisossak.event.EventService
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksClient
import no.nav.sosialhjelp.modia.hentSoknadTittel
import no.nav.sosialhjelp.modia.kommune.KommuneService
import no.nav.sosialhjelp.modia.kommune.KommunenavnService
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.unixToLocalDateTime
import org.springframework.stereotype.Component

@Component
class NoekkelinfoService(
    private val fiksClient: FiksClient,
    private val eventService: EventService,
    private val kommunenavnService: KommunenavnService,
    private val kommuneService: KommuneService,
) {
    fun hentNoekkelInfo(fiksDigisosId: String): SoknadNoekkelinfoResponse {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)
        val model = eventService.createModel(digisosSak)
        val kommunenavn = hentBehandlendekommune(digisosSak.kommunenummer)
        val erPapirSoknad = papirSoknad(digisosSak.originalSoknadNAV)

        val behandlendeNavKontor: NavKontorInformasjon? = model.navKontorHistorikk.lastOrNull()
        log.info("Søknadsstatus=${model.status.name} for $fiksDigisosId")
        return SoknadNoekkelinfoResponse(
            tittel = hentSoknadTittel(digisosSak, model),
            status = model.status,
            sistOppdatert = unixToLocalDateTime(digisosSak.sistEndret).toLocalDate(),
            sendtEllerMottattTidspunkt =
                model.historikk
                    .takeIf {
                        it.isNotEmpty()
                    }?.get(0)
                    ?.tidspunkt
                    ?.toLocalDate(),
            // null hvis papirsøknad og ikke enda mottatt
            navKontor = behandlendeNavKontor?.let { NavKontor(it.navEnhetsnavn, it.navEnhetsnummer) },
            kommunenavn = kommunenavn,
            videresendtHistorikk = leggTilVideresendtInfoHvisNavKontorHistorikkHarFlereElementer(model),
            tidspunktForelopigSvar = model.forelopigSvar?.hendelseTidspunkt,
            papirSoknad = erPapirSoknad,
            kommunenummer = digisosSak.kommunenummer
        )
    }

    private fun hentBehandlendekommune(kommunenummer: String): String =
        kommuneService.getBehandlingsanvarligKommune(kommunenummer) ?: kommunenavnService.hentKommunenavnFor(kommunenummer)

    private fun papirSoknad(originalSoknadNav: OriginalSoknadNAV?): Boolean {
        if (originalSoknadNav == null) {
            return true
        }
        return false
    }

    private fun leggTilVideresendtInfoHvisNavKontorHistorikkHarFlereElementer(model: InternalDigisosSoker): List<VideresendtInfo>? =
        if (model.navKontorHistorikk.size > 1) {
            model.navKontorHistorikk
                .map {
                    VideresendtInfo(
                        type = it.type,
                        tidspunkt = it.tidspunkt.toLocalDate(),
                        navKontor = NavKontor(it.navEnhetsnavn, it.navEnhetsnummer),
                    )
                }
        } else {
            null
        }

    companion object {
        private val log by logger()
    }
}
