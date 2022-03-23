package no.nav.sosialhjelp.modia.rest

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.modia.service.hendelse.HendelseService
import no.nav.sosialhjelp.modia.service.tilgangskontroll.AbacService
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class HendelseController(
    private val hendelseService: HendelseService,
    private val abacService: AbacService
) {

    @PostMapping("/{fiksDigisosId}/hendelser")
    fun hentHendelser(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<List<HendelseResponse>> {
        abacService.harTilgang(ident.fnr, token, "/$fiksDigisosId/hendelser", HttpMethod.POST)

        val hendelser = hendelseService.hentHendelser(fiksDigisosId)
        return ResponseEntity.ok(hendelser)
    }

    data class HendelseResponse(
        val tittel: String,
        val tidspunkt: String,
        val beskrivelse: String?,
        val filbeskrivelse: String?
    )
}
