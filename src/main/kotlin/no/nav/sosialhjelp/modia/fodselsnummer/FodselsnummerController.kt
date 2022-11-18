package no.nav.sosialhjelp.modia.fodselsnummer

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.utils.Ident
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"])
class FodselsnummerController(
    private val fodselsnummerService: FodselsnummerService,
    @Value("\${modia_baseurl}") private val modiaBaseurl: String
) {

    @PostMapping("/fodselsnummer")
    fun setFodselsnummer(@RequestBody ident: Ident): ResponseEntity<*> {
        val fnr = ident.fnr.trim()
        if (fnr.isEmpty()) {
            log.error("Request mangler fnr")
            return ResponseEntity.badRequest().body("Mangler fødselsnummer!")
        }
        val fnrId = fodselsnummerService.setFnrForSalesforce(fnr)
        return ResponseEntity.ok(
            SetFodselsnummerResponse(
                modiaSosialhjelpUrl = "$modiaBaseurl/uuid/$fnrId"
            )
        )
    }

    @GetMapping("/fodselsnummer/{fnrId}")
    fun hentFodselsnummer(@PathVariable fnrId: String): ResponseEntity<String> {
        val fnr = fodselsnummerService.getFnr(fnrId)
        if (fnr.isNullOrEmpty()) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok(fnr)
    }

    companion object {
        private val log by logger()
    }
}
