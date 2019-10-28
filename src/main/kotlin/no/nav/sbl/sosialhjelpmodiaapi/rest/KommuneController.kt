package no.nav.sbl.sosialhjelpmodiaapi.rest

import no.nav.sbl.sosialhjelpmodiaapi.kommune.KommuneService
import no.nav.security.oidc.api.Unprotected
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Unprotected
@RestController
@RequestMapping("/api/v1/innsyn/kommune")
class KommuneController(private val kommuneService: KommuneService) {

    // TODO: kan fjernes (sammen med kommuneservice etc)?

    @GetMapping("/{fiksDigisosId}")
    fun hentKommuneInfo(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<String>{
        val kommuneStatus = kommuneService.hentKommuneStatus(fiksDigisosId, token)

        return ResponseEntity.ok(kommuneStatus.toString())
    }

}
