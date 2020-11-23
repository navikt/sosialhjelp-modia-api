package no.nav.sbl.sosialhjelpmodiaapi.service.hendelse

import no.nav.sbl.sosialhjelpmodiaapi.domain.HendelseResponse
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.service.vedlegg.InternalVedlegg
import no.nav.sbl.sosialhjelpmodiaapi.service.vedlegg.VedleggService
import no.nav.sbl.sosialhjelpmodiaapi.unixToLocalDateTime
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.floor


@Component
class HendelseService(
        private val fiksClient: FiksClient,
        private val eventService: EventService,
        private val vedleggService: VedleggService
) {

    fun hentHendelser(fiksDigisosId: String): List<HendelseResponse> {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)
        val model = eventService.createModel(digisosSak)

        val vedlegg: List<InternalVedlegg> = vedleggService.hentEttersendteVedlegg(digisosSak, model)
        digisosSak.originalSoknadNAV?.timestampSendt?.let { model.leggTilHendelserForOpplastinger(it, vedlegg) }

        model.leggTilHendelserForUtbetalinger()

        val responseList = model.historikk
                .sortedBy { it.tidspunkt }
                .map { HendelseResponse(it.tittel, it.tidspunkt.toString(), it.beskrivelse, it.filbeskrivelse) }
        log.info("Hentet historikk for fiksDigisosId=$fiksDigisosId")
        return responseList.sortedByDescending { it.tidspunkt }
    }

    private fun InternalDigisosSoker.leggTilHendelserForOpplastinger(timestampSoknadSendt: Long, vedlegg: List<InternalVedlegg>) {
        vedlegg
                .filter { it.datoLagtTil!!.isAfter(unixToLocalDateTime(timestampSoknadSendt)) }
                .filter { it.antallFiler > 0 }
                .groupBy { it.datoLagtTil }
                .forEach { (tidspunkt, samtidigOpplastedeVedlegg) ->
                    val antallVedleggForTidspunkt = samtidigOpplastedeVedlegg.sumBy { it.antallFiler }
                    historikk.add(
                            Hendelse("Du har sendt $antallVedleggForTidspunkt vedlegg til NAV", null, tidspunkt!!)
                    )
                }
    }

    private fun InternalDigisosSoker.leggTilHendelserForUtbetalinger() {
        utbetalinger
//                .filterNot { it.status == UtbetalingsStatus.ANNULLERT } // TODO - Finn ut om annullert skal gi melding i historikk
                .groupBy { it.datoHendelse.rundNedTilNaermeste5Minutt() }
                .forEach { (_, grupperteVilkar) ->
                    historikk.add(
                            Hendelse("Utbetalingsplanen din har blitt oppdatert.", null, grupperteVilkar[0].datoHendelse) // TODO - lenke til utbetalingsplan
                    )
                }
    }

    private fun LocalDateTime.rundNedTilNaermeste5Minutt(): LocalDateTime {
        return withMinute((floor(this.minute / 5.0) * 5.0).toInt())
                .truncatedTo(ChronoUnit.MINUTES)
    }

    companion object {
        private val log by logger()
    }
}