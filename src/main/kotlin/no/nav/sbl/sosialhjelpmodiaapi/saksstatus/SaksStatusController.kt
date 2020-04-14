package no.nav.sbl.sosialhjelpmodiaapi.saksstatus

import no.nav.sbl.sosialhjelpmodiaapi.abac.AbacService
import no.nav.sbl.sosialhjelpmodiaapi.domain.Ident
import no.nav.sbl.sosialhjelpmodiaapi.domain.SaksStatusResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus
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
class SaksStatusController(
        private val saksStatusService: SaksStatusService,
        private val abacService: AbacService
) {

    @PostMapping("/{fiksDigisosId}/saksStatus")
    fun hentSaksStatuser(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<List<SaksStatusResponse>> {
        abacService.harTilgang(ident.fnr, token)

        // kan ikke bruke saksbehandlers token til å hente saksStatuser?

        val saksStatuser = saksStatusService.hentSaksStatuser(fiksDigisosId, token)
        if (saksStatuser.isEmpty()) {
            return ResponseEntity(HttpStatus.NO_CONTENT)
        }
        return ResponseEntity.ok(saksStatuser)
    }
}