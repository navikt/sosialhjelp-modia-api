package no.nav.sosialhjelp.modia.rest

import no.nav.sosialhjelp.modia.service.tilgangskontroll.AbacService
import no.nav.sosialhjelp.modia.service.soknadsstatus.SoknadsStatusService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.modia.domain.SoknadsStatus
import org.springframework.http.HttpHeaders.AUTHORIZATION
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
class SoknadsStatusController(
        private val soknadsStatusService: SoknadsStatusService,
        private val abacService: AbacService
) {

    @PostMapping("/{fiksDigisosId}/soknadsStatus")
    fun hentSoknadsStatus(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<SoknadsStatusResponse> {
        abacService.harTilgang(ident.fnr, token)

        val soknadsStatus: SoknadsStatusResponse = soknadsStatusService.hentSoknadsStatus(fiksDigisosId)
        return ResponseEntity.ok().body(soknadsStatus)
    }

    data class SoknadsStatusResponse(
        val status: SoknadsStatus
    )

}