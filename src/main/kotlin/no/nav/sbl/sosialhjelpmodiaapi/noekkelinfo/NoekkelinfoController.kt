package no.nav.sbl.sosialhjelpmodiaapi.noekkelinfo

import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadNoekkelinfoResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "veileder")
@RequestMapping("/api/v1/innsyn/{fnr}")
class NoekkelinfoController(private val noekkelinfoService: NoekkelinfoService) {

    @GetMapping("/{fiksDigisosId}/noekkelinfo", produces = ["application/json;charset=UTF-8"])
    fun hentNoekkelInfo(@PathVariable fnr: String, @PathVariable fiksDigisosId: String, @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String): ResponseEntity<SoknadNoekkelinfoResponse> {
        // sjekk tilgang til fnr
        // kan ikke bruke saksbehandlers token til å hente noekkelinfo for søknad?
        val noekkelinfo = noekkelinfoService.hentNoekkelInfo(fiksDigisosId, token)

        return ResponseEntity.ok().body(noekkelinfo)
    }
}