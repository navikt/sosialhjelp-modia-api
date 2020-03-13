package no.nav.sbl.sosialhjelpmodiaapi.personinfo

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
class PersonInfoController(private val personinfoService: PersoninfoService) {

    @GetMapping("/personinfo", produces = ["application/json;charset=UTF-8"])
    fun hentPersonInfo(@RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<PersoninfoResponse> {
        val personinfoResponse = personinfoService.hentPersoninfo("ident")
        return ResponseEntity.ok(personinfoResponse)
    }
}