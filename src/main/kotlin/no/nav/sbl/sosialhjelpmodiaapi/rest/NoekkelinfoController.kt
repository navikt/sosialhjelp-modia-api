package no.nav.sbl.sosialhjelpmodiaapi.rest

import no.nav.sbl.sosialhjelpmodiaapi.service.tilgangskontroll.AbacService
import no.nav.sbl.sosialhjelpmodiaapi.domain.Ident
import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadNoekkelinfoResponse
import no.nav.sbl.sosialhjelpmodiaapi.service.noekkelinfo.NoekkelinfoService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "veileder")
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class NoekkelinfoController(
        private val noekkelinfoService: NoekkelinfoService,
        private val abacService: AbacService
) {

    @PostMapping("/{fiksDigisosId}/noekkelinfo")
    fun hentNoekkelInfo(@PathVariable fiksDigisosId: String, @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<SoknadNoekkelinfoResponse> {
        abacService.harTilgang(ident.fnr, token)

        val noekkelinfo = noekkelinfoService.hentNoekkelInfo(fiksDigisosId)
        return ResponseEntity.ok().body(noekkelinfo)
    }
}