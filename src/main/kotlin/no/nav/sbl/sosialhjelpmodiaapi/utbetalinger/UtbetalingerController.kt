package no.nav.sbl.sosialhjelpmodiaapi.utbetalinger


import no.nav.sbl.sosialhjelpmodiaapi.abac.AbacService
import no.nav.sbl.sosialhjelpmodiaapi.common.TilgangskontrollException
import no.nav.sbl.sosialhjelpmodiaapi.domain.Ident
import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingerResponse
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders.AUTHORIZATION
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
class UtbetalingerController(
        private val utbetalingerService: UtbetalingerService,
        private val fiksClient: FiksClient,
        private val abacService: AbacService
) {

    @PostMapping("/utbetalinger")
    fun hentUtbetalinger(@RequestHeader(value = AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<List<UtbetalingerResponse>> {
        if (!abacService.harTilgang(ident.fnr, token)) {
            throw TilgangskontrollException("Ingen tilgang til ressurs")
        }

        // kan ikke bruker saksbehandlers token for å hente utbetalinger?

        return ResponseEntity.ok().body(utbetalingerService.hentUtbetalinger(token))
    }

    @PostMapping("/{fiksDigisosId}/utbetalinger")
    fun hentUtbetalingerForDigisosSak(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<List<UtbetalingerResponse>> {
        if (!abacService.harTilgang(ident.fnr, token)) {
            throw TilgangskontrollException("Ingen tilgang til ressurs")
        }

        // kan ikke bruker saksbehandlers token for å hente utbetalinger?

        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId, token)
        return ResponseEntity.ok().body(utbetalingerService.hentUtbetalingerForDigisosSak(digisosSak, token))
    }

}