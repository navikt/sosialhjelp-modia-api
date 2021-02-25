package no.nav.sosialhjelp.modia.rest

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.modia.domain.SendingType
import no.nav.sosialhjelp.modia.domain.SoknadsStatus
import no.nav.sosialhjelp.modia.service.noekkelinfo.NoekkelinfoService
import no.nav.sosialhjelp.modia.service.tilgangskontroll.AbacService
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class NoekkelinfoController(
        private val noekkelinfoService: NoekkelinfoService,
        private val abacService: AbacService
) {

    @PostMapping("/{fiksDigisosId}/noekkelinfo")
    fun hentNoekkelInfo(@PathVariable fiksDigisosId: String, @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<SoknadNoekkelinfoResponse> {
        abacService.harTilgang(ident.fnr, token)

        val noekkelinfo = noekkelinfoService.hentNoekkelInfo(fiksDigisosId)
        return ResponseEntity.ok().body(noekkelinfo)
    }

    data class SoknadNoekkelinfoResponse(
        val tittel: String,
        val status: SoknadsStatus,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val sistOppdatert: LocalDate,
        val saksId: String?,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val sendtEllerMottattTidspunkt: LocalDate,
        val navKontor: NavKontor?,
        val kommunenavn: String,
        val videresendtHistorikk: List<VideresendtInfo>?,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        val tidspunktForelopigSvar: LocalDateTime?
    )

    data class VideresendtInfo(
        val type: SendingType,
        @JsonFormat(pattern = "yyyy-MM-dd")
        val tidspunkt: LocalDate,
        val navKontor: NavKontor
    )

    data class NavKontor(
        val enhetsNavn: String,
        val enhetsNr: String
    )

}