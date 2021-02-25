package no.nav.sosialhjelp.modia.rest

import no.nav.sosialhjelp.modia.client.fiks.FiksClient
import no.nav.sosialhjelp.modia.domain.Ident
import no.nav.sosialhjelp.modia.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.domain.SaksDetaljerResponse
import no.nav.sosialhjelp.modia.domain.SaksListeResponse
import no.nav.sosialhjelp.modia.event.EventService
import no.nav.sosialhjelp.modia.hentSoknadTittel
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.service.oppgave.OppgaveService
import no.nav.sosialhjelp.modia.service.tilgangskontroll.AbacService
import no.nav.sosialhjelp.modia.unixTimestampToDate
import no.nav.sosialhjelp.modia.utils.IntegrationUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class SoknadsoversiktController(
        private val fiksClient: FiksClient,
        private val eventService: EventService,
        private val oppgaveService: OppgaveService,
        private val abacService: AbacService
) {

    @PostMapping("/saker")
    fun hentAlleSaker(@RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<List<SaksListeResponse>> {
        abacService.harTilgang(ident.fnr, token)

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
        abacService.harTilgang(ident.fnr, token)

        val sak = fiksClient.hentDigisosSak(fiksDigisosId)
        val model = eventService.createSoknadsoversiktModel(sak)
        val saksDetaljerResponse = SaksDetaljerResponse(
                fiksDigisosId = sak.fiksDigisosId,
                soknadTittel = hentSoknadTittel(sak, model),
                status = model.status!!,
                harNyeOppgaver = harNyeOppgaver(model, sak.fiksDigisosId),
                harVilkar = harVilkar(model)
        )
        return ResponseEntity.ok().body(saksDetaljerResponse)
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