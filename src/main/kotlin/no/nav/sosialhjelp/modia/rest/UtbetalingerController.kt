package no.nav.sosialhjelp.modia.rest


import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.sosialhjelp.modia.client.fiks.FiksClient
import no.nav.sosialhjelp.modia.service.tilgangskontroll.AbacService
import no.nav.sosialhjelp.modia.service.utbetalinger.UtbetalingerService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.modia.domain.UtbetalingsStatus
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class UtbetalingerController(
        private val utbetalingerService: UtbetalingerService,
        private val fiksClient: FiksClient,
        private val abacService: AbacService
) {

    @PostMapping("/utbetalinger")
    fun hentUtbetalinger(
            @RequestHeader(value = AUTHORIZATION) token: String,
            @RequestBody ident: Ident,
            @RequestParam(defaultValue = "3") month: Int,
            @RequestParam fom: String?,
            @RequestParam tom: String?
    ): ResponseEntity<List<UtbetalingerResponse>> {
        abacService.harTilgang(ident.fnr, token)

        return ResponseEntity.ok().body(
                utbetalingerService.hentAlleUtbetalinger(
                        ident.fnr,
                        month,
                        fom?.let { LocalDate.parse(it, ISO_LOCAL_DATE) },
                        tom?.let { LocalDate.parse(it, ISO_LOCAL_DATE) }
                ))
    }

    @PostMapping("/{fiksDigisosId}/utbetalinger")
    fun hentUtbetalingerForDigisosSak(
            @PathVariable fiksDigisosId: String,
            @RequestHeader(value = AUTHORIZATION) token: String,
            @RequestBody ident: Ident
    ): ResponseEntity<List<UtbetalingerResponse>> {
        abacService.harTilgang(ident.fnr, token)

        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)
        return ResponseEntity.ok().body(utbetalingerService.hentUtbetalingerForDigisosSak(digisosSak))
    }

    data class UtbetalingerResponse(
        val tittel: String?,
        val belop: Double,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val utbetalingEllerForfallDigisosSoker: LocalDate?,
        val status: UtbetalingsStatus,
        val fiksDigisosId: String,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val fom: LocalDate?,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val tom: LocalDate?,
        val mottaker: String?,
        val annenMottaker: Boolean,
        val kontonummer: String?,
        val utbetalingsmetode: String?,
        val harVilkar: Boolean,
        val navKontor: NavKontor?
    )

    data class NavKontor(
        val enhetsNavn: String,
        val enhetsNr: String
    )

}