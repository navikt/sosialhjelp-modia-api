package no.nav.sosialhjelp.modia.soknad.vedlegg

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.security.token.support.core.api.ProtectedWithClaims
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
import java.time.LocalDateTime

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class VedleggController(
    private val vedleggService: VedleggService,
    private val tilgangskontrollService: TilgangskontrollService,
) {
    @PostMapping("/{fiksDigisosId}/vedlegg")
    fun hentVedlegg(
        @PathVariable fiksDigisosId: String,
        @RequestHeader(value = AUTHORIZATION) token: String,
        @RequestBody ident: Ident,
    ): ResponseEntity<List<VedleggResponse>> {
        tilgangskontrollService.harTilgang(ident.fnr, token, "/$fiksDigisosId/vedlegg", HttpMethod.POST)

        val internalVedleggList: List<InternalVedlegg> = vedleggService.hentAlleOpplastedeVedlegg(fiksDigisosId)
        if (internalVedleggList.isEmpty()) {
            return ResponseEntity(HttpStatus.NO_CONTENT)
        }

        val vedleggResponses =
            internalVedleggList
                .map { vedlegg ->
                    VedleggResponse(
                        type = vedlegg.tittelForVeileder ?: vedlegg.type,
                        tilleggsinfo = vedlegg.beskrivelseForVeileder ?: vedlegg.tilleggsinfo,
                        innsendelsesfrist = vedlegg.innsendelsesfrist,
                        datoLagtTil = vedlegg.datoLagtTil,
                        antallVedlegg = vedlegg.antallFiler,
                    )
                }.filter { it.antallVedlegg > 0 }
                .sortedWith(compareByDescending<VedleggResponse> { it.innsendelsesfrist }.thenByDescending { it.datoLagtTil })
        return ResponseEntity.ok(vedleggResponses)
    }

    data class VedleggResponse(
        val type: String,
        val tilleggsinfo: String?,
        @param:JsonFormat(pattern = "yyyy-MM-dd")
        val innsendelsesfrist: LocalDateTime?,
        @param:JsonFormat(pattern = "yyyy-MM-dd")
        val datoLagtTil: LocalDateTime?,
        val antallVedlegg: Int,
    )
}
