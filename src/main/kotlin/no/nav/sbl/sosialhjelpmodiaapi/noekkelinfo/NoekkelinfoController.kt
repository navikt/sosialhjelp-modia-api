package no.nav.sbl.sosialhjelpmodiaapi.noekkelinfo

import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadNoekkelinfoResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/innsyn/")
class NoekkelinfoController(private val noekkelinfoService: NoekkelinfoService) {

    @GetMapping("{fiksDigisosId}/noekkelinfo", produces = ["application/json;charset=UTF-8"])
    fun hentNoekkelInfo(@PathVariable fiksDigisosId: String, @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String): ResponseEntity<SoknadNoekkelinfoResponse> {

        val noekkelinfo = noekkelinfoService.hentNoekkelInfo(fiksDigisosId, token)

        return ResponseEntity.ok().body(noekkelinfo)
    }
}