package no.nav.sbl.sosialhjelpmodiaapi.kommune

import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "veileder")
@RestController
@RequestMapping("/api/v1/innsyn/kommune")
class KommuneController(private val kommuneService: KommuneService) {

    // TODO: kan fjernes (sammen med kommuneservice etc)?

    @GetMapping("/{fiksDigisosId}", produces = ["application/json;charset=UTF-8"])
    fun hentKommuneInfo(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<String> {
        val kommuneStatus = kommuneService.hentKommuneStatus(fiksDigisosId, token)

        return ResponseEntity.ok(kommuneStatus.toString())
    }

}
