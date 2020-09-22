package no.nav.sbl.sosialhjelpmodiaapi.rest

import no.nav.sbl.sosialhjelpmodiaapi.client.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.joda.time.DateTime
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api")
class NavKontorinfoController(
        private val norgClient: NorgClient
) {

    @GetMapping("/kontorinfo", produces = ["application/json;charset=UTF-8"])
    fun hentPersonInfo(@RequestParam(name = "enhetsnr") enhetsnr: String, @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String): ResponseEntity<String> {
        log.info("Debug timing: hentPersonInfo Timing: ${DateTime.now().millis - (MDC.get("input_timing") ?: "-1").toLong()} | ${MDC.get("RequestId")}")
        val enhet = norgClient.hentNavEnhet(enhetsnr)
        if (enhet.sosialeTjenester.isNullOrBlank()) {
            return ResponseEntity.noContent().build()
        }
        return ResponseEntity.ok(enhet.sosialeTjenester)
    }

    companion object {
        private val log by logger()
    }
}
