package no.nav.sosialhjelp.modia.login

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.modia.logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims("azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"])
class LoginController {
    val log by logger()

    @GetMapping("/login")
    fun login(): ResponseEntity<LoginResponse> = ResponseEntity.ok(LoginResponse("ok"))

    data class LoginResponse(
        val melding: String,
    )
}
