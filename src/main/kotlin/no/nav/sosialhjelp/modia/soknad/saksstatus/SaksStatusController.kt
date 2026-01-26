package no.nav.sosialhjelp.modia.soknad.saksstatus

import no.nav.sosialhjelp.modia.tilgang.TilgangskontrollService
import no.nav.sosialhjelp.modia.utils.Ident
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class SaksStatusController(
    private val saksStatusService: SaksStatusService,
    private val tilgangskontrollService: TilgangskontrollService,
) {
    @PostMapping("/{fiksDigisosId}/saksStatus")
    suspend fun hentSaksStatuser(
        @PathVariable fiksDigisosId: String,
        @RequestHeader(value = AUTHORIZATION) token: String,
        @RequestBody ident: Ident,
    ): ResponseEntity<List<SaksStatusResponse>> {
        tilgangskontrollService.harTilgang(ident.fnr, token, "/$fiksDigisosId/saksStatus", HttpMethod.POST)

        val saksStatuser = saksStatusService.hentSaksStatuser(fiksDigisosId)
        if (saksStatuser.isEmpty()) {
            return ResponseEntity(HttpStatus.NO_CONTENT)
        }
        return ResponseEntity.ok(saksStatuser)
    }
}
