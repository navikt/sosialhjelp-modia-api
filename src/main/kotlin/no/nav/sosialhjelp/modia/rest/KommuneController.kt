package no.nav.sosialhjelp.modia.rest

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.modia.service.kommune.KommuneService
import no.nav.sosialhjelp.modia.service.tilgangskontroll.TilgangskontrollService
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

    data class KommuneResponse(
        val erInnsynDeaktivert: Boolean,
        val erInnsynMidlertidigDeaktivert: Boolean,
        val erInnsendingEttersendelseDeaktivert: Boolean,
        val erInnsendingEttersendelseMidlertidigDeaktivert: Boolean,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        val tidspunkt: Date,
        val harNksTilgang: Boolean,
        val behandlingsansvarlig: String?
    )
}
