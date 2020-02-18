package no.nav.sbl.sosialhjelpmodiaapi.utbetalinger


import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingerResponse
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@ProtectedWithClaims(issuer = "veileder")
@RestController
@RequestMapping("/api/v1/innsyn/")
class UtbetalingerController(private val utbetalingerService: UtbetalingerService,
                             private val fiksClient: FiksClient) {

    @GetMapping("utbetalinger", produces = ["application/json;charset=UTF-8"])
    fun hentUtbetalinger(@RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<List<UtbetalingerResponse>> {
        return ResponseEntity.ok().body(utbetalingerService.hentUtbetalinger(token))
    }

    @GetMapping("/{fiksDigisosId}/utbetalinger", produces = ["application/json;charset=UTF-8"])
    fun hentUtbetalingerForDigisosSak(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<List<UtbetalingerResponse>> {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId, token)
        return ResponseEntity.ok().body(utbetalingerService.hentUtbetalinger(digisosSak, token))
    }

}