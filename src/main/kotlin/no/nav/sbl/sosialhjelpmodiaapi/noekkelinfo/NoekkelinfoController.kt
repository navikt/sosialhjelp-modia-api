package no.nav.sbl.sosialhjelpmodiaapi.noekkelinfo

import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadNoekkelinfoResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/innsyn/")
class NoekkelinfoController(private val noekkelinfoService: NoekkelinfoService) {

    @GetMapping("{fiksDigisosId}/noekkelinfo")
    fun hentNoekkelInfo(@PathVariable fiksDigisosId: String, @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String): ResponseEntity<SoknadNoekkelinfoResponse> {

        val noekkelinfo = noekkelinfoService.hentNoekkelInfo(fiksDigisosId, token)

        return ResponseEntity.ok().body(noekkelinfo)
    }
}