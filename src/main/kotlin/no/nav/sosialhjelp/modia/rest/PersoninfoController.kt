package no.nav.sosialhjelp.modia.rest

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.modia.service.personinfo.PersoninfoService
import no.nav.sosialhjelp.modia.service.tilgangskontroll.AbacService
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class PersoninfoController(
    private val personinfoService: PersoninfoService,
    private val abacService: AbacService
) {

    @PostMapping("/personinfo")
    fun hentPersoninfo(@RequestHeader(value = AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<PersoninfoResponse> {
        abacService.harTilgang(ident.fnr, token,"/personinfo", HttpMethod.POST)

        val personinfoResponse = personinfoService.hentPersoninfo(ident.fnr)
        return ResponseEntity.ok(personinfoResponse)
    }

    data class PersoninfoResponse(
        val sammensattNavn: String?,
        val alder: Int?,
        val kjoenn: String?,
        val tlfnr: String?
    )
}
