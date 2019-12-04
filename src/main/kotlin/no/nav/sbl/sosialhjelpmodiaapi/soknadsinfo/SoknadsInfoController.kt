package no.nav.sbl.sosialhjelpmodiaapi.soknadsinfo

import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsInfoResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/innsyn/")
class SoknadsInfoController(private val soknadsInfoService: SoknadsInfoService) {

    @GetMapping("{fiksDigisosId}/soknadsinfo")
    fun hentSoknadsInfo(@PathVariable fiksDigisosId: String, @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String): ResponseEntity<SoknadsInfoResponse> {

        val soknadsInfo = soknadsInfoService.hentSoknadsInfo(fiksDigisosId, token)

        return ResponseEntity.ok().body(soknadsInfo)
    }
}