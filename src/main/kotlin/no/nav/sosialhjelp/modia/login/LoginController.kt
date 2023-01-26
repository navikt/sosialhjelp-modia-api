package no.nav.sosialhjelp.modia.login

import jakarta.servlet.http.HttpServletResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"])
class LoginController {

    @GetMapping("/login")
    fun login(response: HttpServletResponse): ResponseEntity<LoginResponse> {
        return ResponseEntity.ok(LoginResponse("ok"))
    }

    data class LoginResponse(
        val melding: String,
    )
}
