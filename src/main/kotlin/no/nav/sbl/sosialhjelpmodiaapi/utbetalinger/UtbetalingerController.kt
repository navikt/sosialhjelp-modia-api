package no.nav.sbl.sosialhjelpmodiaapi.utbetalinger


import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingerResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "veileder")
@RestController
@RequestMapping("/api/v1/innsyn/")
class UtbetalingerController(private val utbetalingerService: UtbetalingerService) {

    @GetMapping("utbetalinger", produces = ["application/json;charset=UTF-8"])
    fun hentUtbetalinger(@RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<List<UtbetalingerResponse>> {
        // Gitt innlogget bruker
        return ResponseEntity.ok().body(utbetalingerService.hentUtbetalinger(token))
    }

}