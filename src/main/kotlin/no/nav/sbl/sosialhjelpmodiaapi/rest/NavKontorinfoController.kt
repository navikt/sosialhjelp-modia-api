package no.nav.sbl.sosialhjelpmodiaapi.rest

import no.nav.sbl.sosialhjelpmodiaapi.domain.KontorNavnResponse
import no.nav.sbl.sosialhjelpmodiaapi.domain.KontorinfoResponse
import no.nav.sbl.sosialhjelpmodiaapi.service.navkontor.NavKontorService
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
        private val navKontorService: NavKontorService
) {

    @GetMapping("/kontorinfo")
    fun hentNavKontorinfo(@RequestParam(name = "enhetsnr") enhetsnr: String, @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String): ResponseEntity<KontorinfoResponse> {
        val kontorinfo = navKontorService.hentNavKontorinfo(enhetsnr)
        return kontorinfo?.let { ResponseEntity.ok(it) } ?: ResponseEntity.noContent().build()
    }

    @GetMapping("/alleNavKontorinfo")
    fun hentAlleNavKontorinfo(@RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String): ResponseEntity<List<KontorNavnResponse>> {
        val alleEnheter = navKontorService.hentAlleNavKontorinfo()

        return ResponseEntity.ok(alleEnheter)
    }

}
