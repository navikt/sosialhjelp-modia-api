package no.nav.sbl.sosialhjelpmodiaapi.rest

import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsStatusResponse
import no.nav.sbl.sosialhjelpmodiaapi.soknadsstatus.SoknadsStatusService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Unprotected
@RestController
@RequestMapping("/api/v1/innsyn/")
class SoknadsStatusController(private val soknadsStatusService: SoknadsStatusService) {

    @GetMapping("{fiksDigisosId}/soknadsStatus")
    fun hentSoknadsStatus(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<SoknadsStatusResponse> {
        // Gitt innlogget bruker
        val soknadsStatus: SoknadsStatusResponse = soknadsStatusService.hentSoknadsStatus(fiksDigisosId, token)
        return ResponseEntity.ok().body(soknadsStatus)
    }

}