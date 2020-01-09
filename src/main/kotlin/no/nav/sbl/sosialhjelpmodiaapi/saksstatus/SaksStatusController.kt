package no.nav.sbl.sosialhjelpmodiaapi.saksstatus

import no.nav.sbl.sosialhjelpmodiaapi.domain.SaksStatusResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@ProtectedWithClaims(issuer = "veileder")
@RestController
@RequestMapping("/api/v1/innsyn")
class SaksStatusController(private val saksStatusService: SaksStatusService) {

    @GetMapping("/{fiksDigisosId}/saksStatus", produces = [APPLICATION_JSON_VALUE])
    fun hentSaksStatuser(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<List<SaksStatusResponse>> {
        // TODO: sjekk tilgang abac

        val saksStatuser = saksStatusService.hentSaksStatuser(fiksDigisosId, token)
        if (saksStatuser.isEmpty()) {
            return ResponseEntity(HttpStatus.NO_CONTENT)
        }
        return ResponseEntity.ok(saksStatuser)
    }
}