package no.nav.sbl.sosialhjelpmodiaapi.rest

import no.nav.sbl.sosialhjelpmodiaapi.client.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.KontorinfoResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"])
class NavKontorinfoController(
        private val norgClient: NorgClient
) {

    @GetMapping("/kontorinfo")
    fun hentPersonInfo(@RequestParam(name = "enhetsnr") enhetsnr: String, @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String): ResponseEntity<KontorinfoResponse> {
        val enhet = norgClient.hentNavEnhet(enhetsnr)
        if (enhet?.sosialeTjenester.isNullOrBlank()) {
            return ResponseEntity.noContent().build()
        }
        return ResponseEntity.ok(KontorinfoResponse(enhet!!.sosialeTjenester!!))
    }
}