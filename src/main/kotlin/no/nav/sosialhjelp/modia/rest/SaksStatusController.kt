package no.nav.sosialhjelp.modia.rest

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.modia.domain.SaksStatus
import no.nav.sosialhjelp.modia.domain.UtfallVedtak
import no.nav.sosialhjelp.modia.service.saksstatus.SaksStatusService
import no.nav.sosialhjelp.modia.service.tilgangskontroll.AbacService
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
import java.time.LocalDate

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class SaksStatusController(
    private val saksStatusService: SaksStatusService,
    private val abacService: AbacService
) {

    @PostMapping("/{fiksDigisosId}/saksStatus")
    fun hentSaksStatuser(
        @PathVariable fiksDigisosId: String,
        @RequestHeader(value = AUTHORIZATION) token: String,
        @RequestBody ident: Ident
    ): ResponseEntity<List<SaksStatusResponse>> {
        abacService.harTilgang(ident.fnr, token,"/$fiksDigisosId/saksStatus", HttpMethod.POST)

        val saksStatuser = saksStatusService.hentSaksStatuser(fiksDigisosId)
        if (saksStatuser.isEmpty()) {
            return ResponseEntity(HttpStatus.NO_CONTENT)
        }
        return ResponseEntity.ok(saksStatuser)
    }

    data class SaksStatusResponse(
        val tittel: String,
        val status: SaksStatus?,
        val vedtak: List<Vedtak>?,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val datoOpprettet: LocalDate,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val datoAvsluttet: LocalDate?
    ) {

        data class Vedtak(
            @JsonFormat(pattern = "yyyy-MM-dd")
            val vedtakDato: LocalDate,
            val utfall: UtfallVedtak?
        )
    }
}
