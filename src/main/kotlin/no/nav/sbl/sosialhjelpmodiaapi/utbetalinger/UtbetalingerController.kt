package no.nav.sbl.sosialhjelpmodiaapi.utbetalinger


import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingerResponse
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
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
@RequestMapping("/api/v1/innsyn/{fnr}")
class UtbetalingerController(private val utbetalingerService: UtbetalingerService,
                             private val fiksClient: FiksClient) {

    @GetMapping("/utbetalinger", produces = ["application/json;charset=UTF-8"])
    fun hentUtbetalinger(@PathVariable fnr: String, @RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<List<UtbetalingerResponse>> {
        // sjekk tilgang til fnr
        // kan ikke bruker saksbehandlers token for å hente utbetalinger?
        return ResponseEntity.ok().body(utbetalingerService.hentUtbetalinger(token))
    }

    @GetMapping("/{fiksDigisosId}/utbetalinger", produces = ["application/json;charset=UTF-8"])
    fun hentUtbetalingerForDigisosSak(@PathVariable fnr: String, @PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<List<UtbetalingerResponse>> {
        // sjekk tilgang til fnr
        // kan ikke bruker saksbehandlers token for å hente utbetalinger?
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId, token)
        return ResponseEntity.ok().body(utbetalingerService.hentUtbetalingerForDigisosSak(digisosSak, token))
    }

}