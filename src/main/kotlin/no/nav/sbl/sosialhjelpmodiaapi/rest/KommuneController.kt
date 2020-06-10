package no.nav.sbl.sosialhjelpmodiaapi.rest

import no.nav.sbl.sosialhjelpmodiaapi.service.tilgangskontroll.AbacService
import no.nav.sbl.sosialhjelpmodiaapi.domain.Ident
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneService
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
class KommuneController(
        private val kommuneService: KommuneService,
        private val abacService: AbacService
) {

    @PostMapping("/{fiksDigisosId}/kommune")
    fun hentKommuneInfo(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<String> {
        abacService.harTilgang(ident.fnr, token)

        val kommuneStatus = kommuneService.hentKommuneStatus(fiksDigisosId)
        return ResponseEntity.ok(kommuneStatus.toString())
    }

}
