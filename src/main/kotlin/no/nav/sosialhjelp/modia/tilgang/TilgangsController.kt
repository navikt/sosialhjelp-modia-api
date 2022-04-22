package no.nav.sosialhjelp.modia.tilgang

import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api")
class TilgangsController(
    private val tilgangskontrollService: TilgangskontrollService
) {

    @GetMapping("/tilgang")
    fun hentPersoninfo(@RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<String> {
        tilgangskontrollService.harVeilederTilgangTilTjenesten(token, "/tilgang", HttpMethod.GET)
        return ResponseEntity.ok("true")
    }
}
