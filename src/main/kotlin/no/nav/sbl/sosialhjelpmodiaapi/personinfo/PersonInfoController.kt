package no.nav.sbl.sosialhjelpmodiaapi.personinfo

import no.nav.sbl.sosialhjelpmodiaapi.abac.AbacService
import no.nav.sbl.sosialhjelpmodiaapi.common.TilgangskontrollException
import no.nav.sbl.sosialhjelpmodiaapi.domain.Ident
import no.nav.sbl.sosialhjelpmodiaapi.domain.PersoninfoResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "veileder")
@RestController
@RequestMapping("/api/v1/innsyn", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class PersonInfoController(
        private val personinfoService: PersoninfoService,
        private val abacService: AbacService
) {

    @PostMapping("/personinfo")
    fun hentPersonInfo(@RequestHeader(value = AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<PersoninfoResponse> {
        if (!abacService.harTilgang(ident.fnr, token)) {
            throw TilgangskontrollException("Ingen tilgang til ressurs", null)
        }

        val personinfoResponse = personinfoService.hentPersoninfo(ident.fnr)
        return ResponseEntity.ok(personinfoResponse)
    }
}