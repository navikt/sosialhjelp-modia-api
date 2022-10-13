package no.nav.sosialhjelp.modia.fodselsnummer

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.modia.utils.Ident
import no.nav.sosialhjelp.modia.utils.MiljoUtils
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
    private val miljoUtils: MiljoUtils,
    private val fodselsnummerService: FodselsnummerService,
) {

    @PostMapping("/fodselsnummer")
    fun setFodselsnummer(@RequestBody ident: Ident): ResponseEntity<FodselsnummerResponse> {
        val fnr = ident.fnr.trim()
        if (fnr.isEmpty()) {
            return ResponseEntity.badRequest().body(FodselsnummerResponse("Mangler fødselsnummer!"))
        }
        val fnrId = fodselsnummerService.set(fnr)
        return ResponseEntity.ok(FodselsnummerResponse("${hentModiaUrl()}/$fnrId"))
    }

    @GetMapping("/fodselsnummer/{fnrId}")
    fun hentFodselsnummer(@PathVariable fnrId: String): ResponseEntity<FodselsnummerResponse> {
        val fnr = fodselsnummerService.get(fnrId)
        if (fnr.isNullOrEmpty()) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok(FodselsnummerResponse(fnr))
    }

    private fun hentModiaUrl(): String {
        return when {
            miljoUtils.isProfileLocal() -> MODIA_LOCAL_URL
            miljoUtils.isRunningInProd() -> MODIA_PROD_URL
            else -> MODIA_DEV_URL
        }
    }

    companion object {
        private const val MODIA_LOCAL_URL = "http://localhost:3000/sosialhjelp/modia"
        private const val MODIA_DEV_URL = "https://digisos.ekstern.dev.nav.no/sosialhjelp/modia"
        private const val MODIA_PROD_URL = "https://www.nav.no/sosialhjelp/modia"
    }
}
