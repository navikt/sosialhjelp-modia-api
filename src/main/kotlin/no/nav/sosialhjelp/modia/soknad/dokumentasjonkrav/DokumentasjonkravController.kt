package no.nav.sosialhjelp.modia.soknad.dokumentasjonkrav

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.modia.tilgang.TilgangskontrollService
import no.nav.sosialhjelp.modia.utils.Ident
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class DokumentasjonkravController(
    private val dokumentasjonkravService: DokumentasjonkravService,
    private val tilgangskontrollService: TilgangskontrollService,
) {
    @PostMapping("/{fiksDigisosId}/dokumentasjonkrav")
    fun hentOppgaver(
        @PathVariable fiksDigisosId: String,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String,
        @RequestBody ident: Ident,
    ): ResponseEntity<List<DokumentasjonkravResponse>> {
        tilgangskontrollService.harTilgang(ident.fnr, token, "/$fiksDigisosId/dokumentasjonkrav", HttpMethod.POST)
        val dokumentasjonkrav = dokumentasjonkravService.hentDokumentasjonkrav(fiksDigisosId)
        if (dokumentasjonkrav.isEmpty()) {
            return ResponseEntity(HttpStatus.NO_CONTENT)
        }
        return ResponseEntity.ok(dokumentasjonkrav)
    }
}
