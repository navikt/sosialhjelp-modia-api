package no.nav.sbl.sosialhjelpmodiaapi.oppgave

import no.nav.sbl.sosialhjelpmodiaapi.domain.OppgaveResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "veileder")
@RestController
@RequestMapping("/api/v1/innsyn/{fnr}")
class OppgaveController(val oppgaveService: OppgaveService) {

    @GetMapping("/{fiksDigisosId}/oppgaver", produces = ["application/json;charset=UTF-8"])
    fun hentOppgaver(@PathVariable fnr: String, @PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<List<OppgaveResponse>> {
        // sjekk tilgang til fnr
        // kan ikke bruke saksbehandlers token til å hente oppgaver?
        val oppgaver = oppgaveService.hentOppgaver(fiksDigisosId, token)
        if (oppgaver.isEmpty()) {
            return ResponseEntity(HttpStatus.NO_CONTENT)
        }
        return ResponseEntity.ok(oppgaver)
    }
}