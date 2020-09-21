package no.nav.sbl.sosialhjelpmodiaapi.rest

import no.nav.sbl.sosialhjelpmodiaapi.service.tilgangskontroll.AbacService
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksException
import no.nav.sbl.sosialhjelpmodiaapi.domain.Ident
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.SaksDetaljerResponse
import no.nav.sbl.sosialhjelpmodiaapi.domain.SaksListeResponse
import no.nav.sbl.sosialhjelpmodiaapi.domain.SaksStatus
import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsStatus
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.service.oppgave.OppgaveService
import no.nav.sbl.sosialhjelpmodiaapi.service.saksstatus.DEFAULT_TITTEL
import no.nav.sbl.sosialhjelpmodiaapi.unixTimestampToDate
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.joda.time.DateTime
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "veileder")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class SoknadsoversiktController(
        private val fiksClient: FiksClient,
        private val eventService: EventService,
        private val oppgaveService: OppgaveService,
        private val abacService: AbacService
) {

    @PostMapping("/saker")
    fun hentAlleSaker(@RequestHeader headers: MultiValueMap<String, String>, @RequestBody ident: Ident): ResponseEntity<List<SaksListeResponse>> {
        log.info("Debug timing: hentAlleSaker Timing: ${DateTime.now().millis - (MDC.get("input_timing") ?: "-1").toLong()} | ${MDC.get("RequestId")}")
        log.info("Alle headere til hentAlleSaker: $headers")
        val token = headers.getFirst(HttpHeaders.AUTHORIZATION)
        abacService.harTilgang(ident.fnr, token!!)

        val saker = try {
            fiksClient.hentAlleDigisosSaker(ident.fnr)
        } catch (e: FiksException) {
            return ResponseEntity.status(503).build()
        }

        val responselist = saker
                .map { sak ->
                    SaksListeResponse(
                            fiksDigisosId = sak.fiksDigisosId,
                            soknadTittel = "Søknad om økonomisk sosialhjelp",
                            sistOppdatert = unixTimestampToDate(sak.sistEndret),
                            sendt = sak.originalSoknadNAV?.timestampSendt?.let { unixTimestampToDate(it) },
                            kilde = IntegrationUtils.KILDE_INNSYN_API
                    )
                }
        log.info("Hentet alle (${responselist.size}) DigisosSaker for bruker.")

        return ResponseEntity.ok().body(responselist.sortedByDescending { it.sistOppdatert })
    }

    @PostMapping("/{fiksDigisosId}/saksDetaljer")
    fun hentSaksDetaljer(@PathVariable fiksDigisosId: String, @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<SaksDetaljerResponse> {
        val start = DateTime.now().millis
        log.info("Debug timing: hentSaksDetaljer Timing: ${start - (MDC.get("input_timing") ?: "-1").toLong()} | ${MDC.get("RequestId")}")
        abacService.harTilgang(ident.fnr, token)
        val time1 = DateTime.now().millis

        val sak = fiksClient.hentDigisosSak(fiksDigisosId)
        val time2 = DateTime.now().millis
        val model = eventService.createSoknadsoversiktModel(sak)
        log.info("Debug timing: hentSaksDetaljer Abac: ${time1 - start} Sak: ${time2 - time1} Model: ${DateTime.now().millis - time2} | ${MDC.get("RequestId")}")
        val saksDetaljerResponse = SaksDetaljerResponse(
                fiksDigisosId = sak.fiksDigisosId,
                soknadTittel = hentNavn(model),
                status = model.status?.let { mapStatus(it) } ?: "",
                harNyeOppgaver = harNyeOppgaver(model, sak.fiksDigisosId),
                harVilkar = harVilkar(model)
        )
        return ResponseEntity.ok().body(saksDetaljerResponse)
    }

    private fun mapStatus(status: SoknadsStatus): String {
        return if (status == SoknadsStatus.BEHANDLES_IKKE) {
            SoknadsStatus.FERDIGBEHANDLET.name
        } else {
            status.name.replace('_', ' ')
        }
    }

    private fun hentNavn(model: InternalDigisosSoker): String {
        return model.saker.filter { SaksStatus.FEILREGISTRERT != it.saksStatus }.joinToString {
            it.tittel ?: DEFAULT_TITTEL
        }
    }

    private fun harNyeOppgaver(model: InternalDigisosSoker, fiksDigisosId: String): Boolean {
        return when {
            model.oppgaver.isEmpty() -> false
            else -> oppgaveService.hentOppgaver(fiksDigisosId).isNotEmpty()
        }
    }

    private fun harVilkar(model: InternalDigisosSoker): Boolean {
        // forenkle?
        return model.saker
                .any { sak ->
                    sak.utbetalinger
                            .flatMap { utbetaling -> utbetaling.vilkar }
                            .any { vilkar -> !vilkar.oppfyllt }
                }
    }

    companion object {
        private val log by logger()
    }
}
