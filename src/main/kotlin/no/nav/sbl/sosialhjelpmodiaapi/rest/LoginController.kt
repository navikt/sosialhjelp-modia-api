package no.nav.sbl.sosialhjelpmodiaapi.rest

import no.nav.sbl.sosialhjelpmodiaapi.domain.LoginResponse
import no.nav.sbl.sosialhjelpmodiaapi.utils.MiljoUtils
import no.nav.sbl.sosialhjelpmodiaapi.utils.TokenUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"])
class LoginController(
    private val tokenUtils: TokenUtils,
    private val miljoUtils: MiljoUtils
) {

    @GetMapping("/login")
    fun login(response: HttpServletResponse): ResponseEntity<LoginResponse> {

        val msgraphAccessToken = tokenUtils.hentTokenMedGraphScope()
        val msgraphCookie = Cookie(MSGRAPH_COOKIE_NAME, msgraphAccessToken)
        msgraphCookie.isHttpOnly = true
        msgraphCookie.secure = !miljoUtils.isProfileMockOrLocal()
        msgraphCookie.path = "/"
        response.addCookie(msgraphCookie)
        return ResponseEntity.ok(LoginResponse("ok"))
    }

    companion object {
        const val MSGRAPH_COOKIE_NAME = "isso-accesstoken" // NB: Navnet "isso-accesstoken" kreves av modiacontextholder.
    }
}
