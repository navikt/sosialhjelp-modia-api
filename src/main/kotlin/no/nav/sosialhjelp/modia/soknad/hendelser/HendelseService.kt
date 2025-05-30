package no.nav.sosialhjelp.modia.soknad.hendelser

import no.nav.sosialhjelp.modia.digisossak.domain.Hendelse
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.UtbetalingsStatus
import no.nav.sosialhjelp.modia.digisossak.event.EventService
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksClient
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.soknad.vedlegg.InternalVedlegg
import no.nav.sosialhjelp.modia.soknad.vedlegg.VedleggService
import no.nav.sosialhjelp.modia.unixToLocalDateTime
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.floor

@Component
class HendelseService(
    private val fiksClient: FiksClient,
    private val eventService: EventService,
    private val vedleggService: VedleggService,
) {
    fun hentHendelser(fiksDigisosId: String): List<HendelseResponse> {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)
        val model = eventService.createModel(digisosSak)

        val vedlegg: List<InternalVedlegg> = vedleggService.hentEttersendteVedlegg(digisosSak, model)
        digisosSak.originalSoknadNAV?.timestampSendt?.let { model.leggTilHendelserForOpplastinger(it, vedlegg) }

        model.leggTilHendelserForUtbetalinger()

        val responseList =
            model.historikk
                .sortedBy { it.tidspunkt }
                .map { HendelseResponse(it.tittel, it.tidspunkt.toString(), it.beskrivelse, it.filbeskrivelse) }
        log.info("Hentet historikk for fiksDigisosId=$fiksDigisosId")
        return responseList.sortedByDescending { it.tidspunkt }
    }

    private fun InternalDigisosSoker.leggTilHendelserForOpplastinger(
        timestampSoknadSendt: Long,
        vedlegg: List<InternalVedlegg>,
    ) {
        vedlegg
            .filter { it.datoLagtTil!!.isAfter(unixToLocalDateTime(timestampSoknadSendt)) }
            .filter { it.antallFiler > 0 }
            .groupBy { it.datoLagtTil }
            .forEach { (tidspunkt, samtidigOpplastedeVedlegg) ->
                val antallVedleggForTidspunkt = samtidigOpplastedeVedlegg.sumOf { it.antallFiler }
                historikk.add(
                    Hendelse("Du har sendt $antallVedleggForTidspunkt vedlegg til Nav", null, tidspunkt!!),
                )
            }
    }

    // TODO - lenke til utbetalingsplan
    private fun InternalDigisosSoker.leggTilHendelserForUtbetalinger() {
        utbetalinger
            .filter { it.status == UtbetalingsStatus.UTBETALT }
            .groupBy { it.datoHendelse.rundNedTilNaermeste5Minutt() }
            .forEach { (_, grupperteVilkar) ->
                historikk.add(
                    Hendelse(
                        "Dine utbetalinger har blitt oppdatert",
                        null,
                        grupperteVilkar[0].datoHendelse,
                    ),
                )
            }
    }

    private fun LocalDateTime.rundNedTilNaermeste5Minutt(): LocalDateTime =
        withMinute((floor(this.minute / 5.0) * 5.0).toInt())
            .truncatedTo(ChronoUnit.MINUTES)

    companion object {
        private val log by logger()
    }
}
