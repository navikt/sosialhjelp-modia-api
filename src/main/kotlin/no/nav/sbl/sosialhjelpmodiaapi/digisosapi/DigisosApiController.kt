package no.nav.sbl.sosialhjelpmodiaapi.digisosapi

import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.SakResponse
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.hentSoknadTittel
import no.nav.sbl.sosialhjelpmodiaapi.oppgave.OppgaveService
import no.nav.sbl.sosialhjelpmodiaapi.unixToLocalDateTime
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.KILDE_INNSYN_API
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "veileder")
@RestController
@RequestMapping("/api/v1/digisosapi")
class DigisosApiController(private val fiksClient: FiksClient,
                           private val eventService: EventService,
                           private val oppgaveService: OppgaveService) {

    @GetMapping("/saker")
    fun hentAlleSaker(@RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String): ResponseEntity<List<SakResponse>> {
        val saker = fiksClient.hentAlleDigisosSaker(token)

        val responselist = saker
                .map { digisosSak ->
                    val model = eventService.createModel(digisosSak, token)

                    SakResponse(
                            digisosSak.fiksDigisosId,
                            hentSoknadTittel(digisosSak, model),
                            model.status.toString(),
                            unixToLocalDateTime(digisosSak.sistEndret),
                            hentAntallNyeOppgaver(model, digisosSak.fiksDigisosId, token),
                            KILDE_INNSYN_API
                    )
                }

        return ResponseEntity.ok().body(responselist)
    }

    private fun hentAntallNyeOppgaver(model: InternalDigisosSoker, fiksDigisosId: String, token: String): Int? {
        return when {
            model.oppgaver.isEmpty() -> null
            else -> oppgaveService.hentOppgaver(fiksDigisosId, token).size
        }
    }

}
