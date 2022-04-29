package no.nav.sosialhjelp.modia.utbetalinger

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.modia.client.fiks.FiksClient
import no.nav.sosialhjelp.modia.tilgang.TilgangskontrollService
import no.nav.sosialhjelp.modia.utils.Ident
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
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
    private val tilgangskontrollService: TilgangskontrollService
) {

    @PostMapping("/utbetalinger")
    fun hentUtbetalinger(
        @RequestHeader(value = AUTHORIZATION) token: String,
        @RequestBody ident: Ident,
        @RequestParam(defaultValue = "3") months: Int,
        @RequestParam fom: String?,
        @RequestParam tom: String?
    ): ResponseEntity<List<UtbetalingerResponse>> {
        tilgangskontrollService.harTilgang(ident.fnr, token, "/utbetalinger", HttpMethod.POST)

        return ResponseEntity.ok().body(
            utbetalingerService.hentAlleUtbetalinger(
                ident.fnr,
                months,
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
        tilgangskontrollService.harTilgang(ident.fnr, token, "/$fiksDigisosId/utbetalinger", HttpMethod.POST)

        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)
        return ResponseEntity.ok().body(utbetalingerService.hentUtbetalingerForDigisosSak(digisosSak))
    }
}
