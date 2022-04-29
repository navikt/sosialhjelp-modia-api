package no.nav.sosialhjelp.modia.kommune

import no.nav.security.token.support.core.api.ProtectedWithClaims
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
import org.springframework.web.bind.annotation.RestController
import java.util.Date

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class KommuneController(
    private val kommuneService: KommuneService,
    private val tilgangskontrollService: TilgangskontrollService
) {

    @PostMapping("/kommuner/{kommunenummer}")
    fun hentKommuneInfo(@PathVariable kommunenummer: String, @RequestHeader(value = AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<KommuneResponse> {
        tilgangskontrollService.harTilgang(ident.fnr, token, "/kommuner/$kommunenummer", HttpMethod.POST)

        val kommuneInfo = kommuneService.get(kommunenummer)
        return ResponseEntity.ok(
            KommuneResponse(
                erInnsynDeaktivert = !kommuneInfo.kanOppdatereStatus,
                erInnsynMidlertidigDeaktivert = kommuneInfo.harMidlertidigDeaktivertOppdateringer,
                erInnsendingEttersendelseDeaktivert = !kommuneInfo.kanMottaSoknader,
                erInnsendingEttersendelseMidlertidigDeaktivert = kommuneInfo.harMidlertidigDeaktivertMottak,
                tidspunkt = Date(),
                harNksTilgang = kommuneInfo.harNksTilgang,
                behandlingsansvarlig = kommuneInfo.behandlingsansvarlig
            )
        )
    }
}
