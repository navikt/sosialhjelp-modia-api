package no.nav.sbl.sosialhjelpmodiaapi.kontorinfo

import no.nav.sbl.sosialhjelpmodiaapi.norg.NorgClient
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@ProtectedWithClaims(issuer = "veileder")
@RestController
@RequestMapping("/api/v1/innsyn")
class NavKontorinfoController(private val norgClient: NorgClient) {

    @GetMapping("/kontorinfo", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentPersonInfo(@RequestParam(name = "enhetsnr") enhetsnr: String, @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String): ResponseEntity<String> {
        val enhet = norgClient.hentNavEnhet(enhetsnr)
        if (enhet.sosialeTjenester.isNullOrBlank()) {
            return ResponseEntity.noContent().build()
        }
        return ResponseEntity.ok(enhet.sosialeTjenester)
    }
}