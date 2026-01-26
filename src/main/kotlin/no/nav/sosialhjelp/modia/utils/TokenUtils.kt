package no.nav.sosialhjelp.modia.utils

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

interface TokenUtils {
    fun hentNavIdentForInnloggetBruker(): String
}

@Profile("!(mock-alt | local)&gcp")
@Component
class TokenUtilsImpl(
    private val tokenValidationContextHolder: TokenValidationContextHolder,
) : TokenUtils {
    override fun hentNavIdentForInnloggetBruker(): String {
        val claims = tokenValidationContextHolder.getTokenValidationContext().getClaims("azuread")
        if (claims.get("NAVident") == null) {
            error("NAVident ikke funnet i token claims")
        }
        return claims.getStringClaim("NAVident")
    }
}

@Profile("(mock-alt | local)")
@Component
class MockTokenUtils : TokenUtils {
    override fun hentNavIdentForInnloggetBruker(): String = "Z123456"
}
