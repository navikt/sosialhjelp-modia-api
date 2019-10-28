package no.nav.sbl.sosialhjelpmodiaapi.rest


import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingerResponse
import no.nav.sbl.sosialhjelpmodiaapi.utbetalinger.UtbetalingerService
import no.nav.security.oidc.api.Unprotected
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Unprotected
@RestController
@RequestMapping("/api/v1/innsyn/")
class UtbetalingerController(private val utbetalingerService: UtbetalingerService) {

    @GetMapping("utbetalinger")
    fun hentUtbetalinger(@RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<List<UtbetalingerResponse>> {
        // TODO: sjekk tilgang abac

        return ResponseEntity.ok().body(utbetalingerService.hentUtbetalinger(token))
    }

}