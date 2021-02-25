package no.nav.sosialhjelp.modia.rest

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.modia.domain.Ident
import no.nav.sosialhjelp.modia.service.oppgave.OppgaveService
import no.nav.sosialhjelp.modia.service.tilgangskontroll.AbacService
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class OppgaveController(
        private val oppgaveService: OppgaveService,
        private val abacService: AbacService
) {

    @PostMapping("/{fiksDigisosId}/oppgaver")
    fun hentOppgaver(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<List<OppgaveResponse>> {
        abacService.harTilgang(ident.fnr, token)

        val oppgaver = oppgaveService.hentOppgaver(fiksDigisosId)
        if (oppgaver.isEmpty()) {
            return ResponseEntity(HttpStatus.NO_CONTENT)
        }
        return ResponseEntity.ok(oppgaver)
    }

    data class OppgaveResponse(
        val dokumenttype: String,
        val tilleggsinformasjon: String?,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val innsendelsesfrist: LocalDate?,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val vedleggDatoLagtTil: LocalDate?,
        val antallVedlegg: Int,
        val erFraInnsyn: Boolean
    )
}