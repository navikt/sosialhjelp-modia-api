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
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "veileder")
@RestController
@RequestMapping("/api/v1/innsyn", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class SoknadsoversiktController(
        private val fiksClient: FiksClient,
        private val eventService: EventService,
        private val oppgaveService: OppgaveService,
        private val abacService: AbacService
) {

    @PostMapping("/saker")
    fun hentAlleSaker(@RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<List<SaksListeResponse>> {
        abacService.harTilgang(ident.fnr, token)

        // kan ikke bruke saksbehandlers token til å hente alle DigisosSaker for søker?

        val saker = try {
            fiksClient.hentAlleDigisosSaker(token, ident.fnr)
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
        abacService.harTilgang(ident.fnr, token)

        // kan ikke bruke saksbehandlers token til å hente saksDetaljer for søknad?
        val sak = fiksClient.hentDigisosSak(fiksDigisosId, token)
        val model = eventService.createSoknadsoversiktModel(token, sak)
        val saksDetaljerResponse = SaksDetaljerResponse(
                fiksDigisosId = sak.fiksDigisosId,
                soknadTittel = hentNavn(model),
                status = model.status?.let { mapStatus(it) } ?: "",
                harNyeOppgaver = harNyeOppgaver(model, sak.fiksDigisosId, token),
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

    private fun harNyeOppgaver(model: InternalDigisosSoker, fiksDigisosId: String, token: String): Boolean {
        return when {
            model.oppgaver.isEmpty() -> false
            else -> oppgaveService.hentOppgaver(fiksDigisosId, token).isNotEmpty()
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