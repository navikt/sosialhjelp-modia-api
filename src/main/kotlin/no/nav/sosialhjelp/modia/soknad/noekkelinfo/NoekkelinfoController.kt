package no.nav.sosialhjelp.modia.soknad.noekkelinfo

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.modia.rest.Ident
import no.nav.sosialhjelp.modia.tilgang.TilgangskontrollService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class NoekkelinfoController(
    private val noekkelinfoService: NoekkelinfoService,
    private val tilgangskontrollService: TilgangskontrollService
) {

    @PostMapping("/{fiksDigisosId}/noekkelinfo")
    fun hentNoekkelInfo(@PathVariable fiksDigisosId: String, @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<SoknadNoekkelinfoResponse> {
        tilgangskontrollService.harTilgang(ident.fnr, token, "/$fiksDigisosId/noekkelinfo", HttpMethod.POST)

        val noekkelinfo = noekkelinfoService.hentNoekkelInfo(fiksDigisosId)
        return ResponseEntity.ok().body(noekkelinfo)
    }
}
