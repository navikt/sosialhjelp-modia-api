package no.nav.sbl.sosialhjelpmodiaapi.rest

import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.Ident
import no.nav.sbl.sosialhjelpmodiaapi.domain.UtbetalingerResponse
import no.nav.sbl.sosialhjelpmodiaapi.service.tilgangskontroll.AbacService
import no.nav.sbl.sosialhjelpmodiaapi.service.utbetalinger.UtbetalingerService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class UtbetalingerController(
    private val utbetalingerService: UtbetalingerService,
    private val fiksClient: FiksClient,
    private val abacService: AbacService
) {

    @PostMapping("/utbetalinger")
    fun hentUtbetalinger(
        @RequestHeader(value = AUTHORIZATION) token: String,
        @RequestBody ident: Ident,
        @RequestParam(defaultValue = "3") month: Int,
        @RequestParam fom: String?,
        @RequestParam tom: String?
    ): ResponseEntity<List<UtbetalingerResponse>> {
        abacService.harTilgang(ident.fnr, token)

        return ResponseEntity.ok().body(
            utbetalingerService.hentAlleUtbetalinger(
                ident.fnr,
                month,
                fom?.let { LocalDate.parse(it, ISO_LOCAL_DATE) },
                tom?.let { LocalDate.parse(it, ISO_LOCAL_DATE) }
            )
        )
    }

    @PostMapping("/{fiksDigisosId}/utbetalinger")
    fun hentUtbetalingerForDigisosSak(
        @PathVariable fiksDigisosId: String,
        @RequestHeader(value = AUTHORIZATION) token: String,
        @RequestBody ident: Ident
    ): ResponseEntity<List<UtbetalingerResponse>> {
        abacService.harTilgang(ident.fnr, token)

        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)
        return ResponseEntity.ok().body(utbetalingerService.hentUtbetalingerForDigisosSak(digisosSak))
    }
}
