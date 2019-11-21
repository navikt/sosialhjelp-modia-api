package no.nav.sbl.sosialhjelpmodiaapi.vedlegg

import no.nav.sbl.sosialhjelpmodiaapi.domain.VedleggResponse
import no.nav.sbl.sosialhjelpmodiaapi.vedlegg.VedleggService.InternalVedlegg
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Unprotected
@RestController
@RequestMapping("/api/v1/innsyn")
class VedleggController(private val vedleggService: VedleggService) {

    @GetMapping("/{fiksDigisosId}/vedlegg", produces = [APPLICATION_JSON_VALUE])
    fun hentVedlegg(@PathVariable fiksDigisosId: String, @RequestHeader(value = AUTHORIZATION) token: String): ResponseEntity<List<VedleggResponse>> {
        val internalVedleggList: List<InternalVedlegg> = vedleggService.hentAlleOpplastedeVedlegg(fiksDigisosId, token)
        if (internalVedleggList.isEmpty()) {
            return ResponseEntity(HttpStatus.NO_CONTENT)
        }

        val vedleggResponses = internalVedleggList
                .flatMap { vedlegg ->
                    if (vedlegg.antallFiler == 0) {
                        return@flatMap listOf(VedleggResponse(
                                vedlegg.type,
                                vedlegg.tilleggsinfo,
                                vedlegg.innsendelsesfrist,
                                vedlegg.datoLagtTil))
                    }
                    (0 until vedlegg.antallFiler)
                            .map {
                                VedleggResponse(
                                        vedlegg.type,
                                        vedlegg.tilleggsinfo,
                                        vedlegg.innsendelsesfrist,
                                        vedlegg.datoLagtTil)
                            }
                }
                .sortedWith(compareByDescending<VedleggResponse> { it.innsendelsesfrist }.thenByDescending { it.datoLagtTil })
        return ResponseEntity.ok(vedleggResponses)
    }
}