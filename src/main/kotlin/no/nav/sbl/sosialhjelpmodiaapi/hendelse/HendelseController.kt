package no.nav.sbl.sosialhjelpmodiaapi.hendelse

import no.nav.sbl.sosialhjelpmodiaapi.domain.HendelseResponse
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Unprotected
@RestController
@RequestMapping("/api/v1/innsyn")
class HendelseController(val hendelseService: HendelseService) {

    @GetMapping("/{fiksDigisosId}/hendelser", produces = [APPLICATION_JSON_VALUE])
    fun hentHendelser(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<List<HendelseResponse>> {
        val hendelser = hendelseService.hentHendelser(fiksDigisosId, token)
        return ResponseEntity.ok(hendelser)
    }
}