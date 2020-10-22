package no.nav.sbl.sosialhjelpmodiaapi.rest

import no.nav.sbl.sosialhjelpmodiaapi.utils.TokenUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class TestController(
        private val tokenUtils: TokenUtils
) {

    @GetMapping("/navident")
    fun hentNavident(@RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String): ResponseEntity<String> {
        val navIdent = tokenUtils.getInnloggetNavIdent()
        return ResponseEntity.ok().body(navIdent)
    }
}