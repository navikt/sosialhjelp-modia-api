package no.nav.sosialhjelp.modia.tilgang

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class TilgangsController(
    private val tilgangskontrollService: TilgangskontrollService,
) {
    @GetMapping("/tilgang")
    fun hentPersoninfo(): ResponseEntity<String> = ResponseEntity.ok("true")
}
