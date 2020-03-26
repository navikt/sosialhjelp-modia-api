package no.nav.sbl.sosialhjelpmodiaapi.vedlegg

import no.nav.sbl.sosialhjelpmodiaapi.abac.AbacService
import no.nav.sbl.sosialhjelpmodiaapi.common.TilgangskontrollException
import no.nav.sbl.sosialhjelpmodiaapi.domain.Ident
import no.nav.sbl.sosialhjelpmodiaapi.domain.VedleggResponse
import no.nav.sbl.sosialhjelpmodiaapi.vedlegg.VedleggService.InternalVedlegg
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "veileder")
@RestController
@RequestMapping("/api/v1/innsyn", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class VedleggController(
        private val vedleggService: VedleggService,
        private val abacService: AbacService
) {

    @PostMapping("/{fiksDigisosId}/vedlegg")
    fun hentVedlegg(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<List<VedleggResponse>> {
        if (!abacService.harTilgang(ident.fnr, token)) {
            throw TilgangskontrollException("Ingen tilgang til ressurs", null)
        }

        // kan ikke bruker saksbehandlers token for å hente vedleggsinfo?

        val internalVedleggList: List<InternalVedlegg> = vedleggService.hentAlleOpplastedeVedlegg(fiksDigisosId, token)
        if (internalVedleggList.isEmpty()) {
            return ResponseEntity(HttpStatus.NO_CONTENT)
        }

        val vedleggResponses = internalVedleggList
                .map { vedlegg ->
                    VedleggResponse(
                            type = vedlegg.type,
                            tilleggsinfo = vedlegg.tilleggsinfo,
                            innsendelsesfrist = vedlegg.innsendelsesfrist,
                            datoLagtTil = vedlegg.datoLagtTil,
                            antallVedlegg = vedlegg.antallFiler
                    )
                }
                .sortedWith(compareByDescending<VedleggResponse> { it.innsendelsesfrist }.thenByDescending { it.datoLagtTil })
        return ResponseEntity.ok(vedleggResponses)
    }
}