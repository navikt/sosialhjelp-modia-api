package no.nav.sbl.sosialhjelpmodiaapi.rest

import no.nav.sbl.sosialhjelpmodiaapi.service.tilgangskontroll.AbacService
import no.nav.sbl.sosialhjelpmodiaapi.domain.Ident
import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsStatusResponse
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.service.soknadsstatus.SoknadsStatusService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.joda.time.DateTime
import org.slf4j.MDC
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "veileder")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class SoknadsStatusController(
        private val soknadsStatusService: SoknadsStatusService,
        private val abacService: AbacService
) {

    @PostMapping("/{fiksDigisosId}/soknadsStatus")
    fun hentSoknadsStatus(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<SoknadsStatusResponse> {
        log.info("Debug timing: hentSoknadsStatus Timing: ${DateTime.now().millis - (MDC.get("input_timing") ?: "-1").toLong()}")
        abacService.harTilgang(ident.fnr, token)

        val soknadsStatus: SoknadsStatusResponse = soknadsStatusService.hentSoknadsStatus(fiksDigisosId)
        return ResponseEntity.ok().body(soknadsStatus)
    }

    companion object {
        private val log by logger()
    }
}
