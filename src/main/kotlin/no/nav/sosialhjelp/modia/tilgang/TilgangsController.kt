package no.nav.sosialhjelp.modia.tilgang

import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api")
class TilgangsController(
    private val tilgangskontrollService: TilgangskontrollService,
) {
    @GetMapping("/tilgang")
    fun hentPersoninfo(): ResponseEntity<String> = ResponseEntity.ok("true")
}
