package no.nav.sbl.sosialhjelpmodiaapi.hendelse

import no.nav.sbl.sosialhjelpmodiaapi.domain.HendelseResponse
import no.nav.sbl.sosialhjelpmodiaapi.domain.Ident
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders.AUTHORIZATION
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
class HendelseController(val hendelseService: HendelseService) {

    @PostMapping("/{fiksDigisosId}/hendelser")
    fun hentHendelser(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<List<HendelseResponse>> {
        // sjekk tilgang til fnr
        // kan ikke bruke saksbehandlers token for å hente hendelser?
        val hendelser = hendelseService.hentHendelser(fiksDigisosId, token)
        return ResponseEntity.ok(hendelser)
    }
}