package no.nav.sbl.sosialhjelpmodiaapi.personinfo

import no.nav.sbl.sosialhjelpmodiaapi.domain.PersonInfoResponse
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
class PersonInfoController(private val personInfoService: PersonInfoService) {

    @GetMapping("/personInfo", produces = ["application/json;charset=UTF-8"])
    fun hentPersonInfo(@RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<PersonInfoResponse> {
        val personInfoResponse = personInfoService.hentPersonInfo("ident")
        return ResponseEntity.ok(personInfoResponse)
    }
}