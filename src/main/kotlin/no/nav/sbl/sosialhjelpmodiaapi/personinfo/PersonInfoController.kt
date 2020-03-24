package no.nav.sbl.sosialhjelpmodiaapi.personinfo

import no.nav.sbl.sosialhjelpmodiaapi.abac.AbacService
import no.nav.sbl.sosialhjelpmodiaapi.abac.annotation.Abac
import no.nav.sbl.sosialhjelpmodiaapi.common.TilgangskontrollException
import no.nav.sbl.sosialhjelpmodiaapi.domain.PersoninfoResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "veileder")
@RestController
@RequestMapping("/api/v1/innsyn")
class PersonInfoController(private val personinfoService: PersoninfoService,
                           private val abacService: AbacService) {

    @GetMapping("/personinfo", produces = ["application/json;charset=UTF-8"])
    fun hentPersonInfo(@RequestHeader(value = AUTHORIZATION) @Abac token: String): ResponseEntity<PersoninfoResponse> {
        // hardkodet testbruker som saksbehandler med tilgang
        val testbrukerNatalie = "26104500284"

        if (!abacService.harTilgang(token)) {
            throw TilgangskontrollException("ingen tilgang til ressurs", null)
        }
        val personinfoResponse = personinfoService.hentPersoninfo(testbrukerNatalie)
        return ResponseEntity.ok(personinfoResponse)
    }
}