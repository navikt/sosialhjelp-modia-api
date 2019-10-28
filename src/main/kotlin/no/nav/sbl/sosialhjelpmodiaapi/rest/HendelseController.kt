package no.nav.sbl.sosialhjelpmodiaapi.rest

import no.nav.sbl.sosialhjelpmodiaapi.domain.HendelseResponse
import no.nav.sbl.sosialhjelpmodiaapi.hendelse.HendelseService
import no.nav.security.oidc.api.Unprotected
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Unprotected
@RestController
@RequestMapping("/api/v1/innsyn")
class HendelseController(val hendelseService: HendelseService) {

    @GetMapping("/{fiksDigisosId}/hendelser", produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun hentHendelser(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<List<HendelseResponse>> {
        // TODO: sjekk tilgang via abac

        val hendelser = hendelseService.hentHendelser(fiksDigisosId, token)
        return ResponseEntity.ok(hendelser)
    }
}