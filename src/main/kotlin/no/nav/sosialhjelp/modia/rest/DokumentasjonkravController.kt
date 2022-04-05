package no.nav.sosialhjelp.modia.rest

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.modia.service.dokumentasjonkrav.DokumentasjonkravService
import no.nav.sosialhjelp.modia.service.tilgangskontroll.AbacService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class DokumentasjonkravController(
    private val dokumentasjonkravService: DokumentasjonkravService,
    private val abacService: AbacService
) {

    @PostMapping("/{fiksDigisosId}/dokumentasjonkrav")
    fun hentOppgaver(
        @PathVariable fiksDigisosId: String,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String,
        @RequestBody ident: Ident
    ): ResponseEntity<List<DokumentasjonkravController.DokumentasjonkravResponse>> {
        abacService.harTilgang(ident.fnr, token)

        val dokumentasjonkrav = dokumentasjonkravService.hentDokumentasjonkrav(fiksDigisosId)
        if (dokumentasjonkrav.isEmpty()) {
            return ResponseEntity(HttpStatus.NO_CONTENT)
        }
        return ResponseEntity.ok(dokumentasjonkrav)
    }

    data class DokumentasjonkravResponse(
        val referanse: String,
        val sakstittel: String?,
        val status: String,
        val utbetalingsbeskrivelse: String?,
        val antallVedlegg: Int,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val innsendelsesfrist: LocalDate?,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val vedleggDatoLagtTil: LocalDate?,
    )
}