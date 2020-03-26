package no.nav.sbl.sosialhjelpmodiaapi.soknadsstatus

import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsStatusResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "veileder")
@RestController
@RequestMapping("/api/v1/innsyn/{fnr}")
class SoknadsStatusController(private val soknadsStatusService: SoknadsStatusService) {

    @GetMapping("/{fiksDigisosId}/soknadsStatus", produces = ["application/json;charset=UTF-8"])
    fun hentSoknadsStatus(@PathVariable fnr: String, @PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<SoknadsStatusResponse> {
        // sjekk tilgang til fnr
        // kan ikke bruke saksbehandlers token til å hente soknadsStatus?
        val soknadsStatus: SoknadsStatusResponse = soknadsStatusService.hentSoknadsStatus(fiksDigisosId, token)
        return ResponseEntity.ok().body(soknadsStatus)
    }

}