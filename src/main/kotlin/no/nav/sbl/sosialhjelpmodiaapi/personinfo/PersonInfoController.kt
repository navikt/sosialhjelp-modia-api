package no.nav.sbl.sosialhjelpmodiaapi.personinfo

import no.nav.sbl.sosialhjelpmodiaapi.abac.AbacService
import no.nav.sbl.sosialhjelpmodiaapi.common.TilgangskontrollException
import no.nav.sbl.sosialhjelpmodiaapi.domain.PersoninfoResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "veileder")
@RestController
@RequestMapping("/api/v1/innsyn")
@Validated
class PersonInfoController(private val personinfoService: PersoninfoService,
                           private val abacService: AbacService) {

    @GetMapping("/personinfo", produces = ["application/json;charset=UTF-8"])
    fun hentPersonInfo(@RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<PersoninfoResponse> {

        val testbrukerNatalie = "26104500284"
        val testbrukerLotte = "17108102454" // kode7 -> gir Deny

        // TODO: Finne ut hvor fnr skal komme fra

        if (abacService.harTilgang(testbrukerNatalie, token)) {
            val personinfoResponse = personinfoService.hentPersoninfo(testbrukerNatalie)
            return ResponseEntity.ok(personinfoResponse)
        }

        throw TilgangskontrollException("Ingen tilgang til ressurs", null)
    }
}