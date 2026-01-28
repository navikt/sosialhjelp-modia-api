package no.nav.sosialhjelp.modia.utils

import org.springframework.context.annotation.Profile
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

interface TokenUtils {
    fun hentNavIdentForInnloggetBruker(): String
}

@Profile("gcp&!mock-alt")
@Component
class TokenUtilsImpl : TokenUtils {
    override fun hentNavIdentForInnloggetBruker(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        val jwt =
            authentication?.principal as? Jwt
                ?: error("No JWT token found in security context")

        return jwt.getClaimAsString("NAVident")
            ?: error("NAVident ikke funnet i token claims")
    }
}

@Profile("!gcp|mock-alt")
@Component
class MockTokenUtils : TokenUtils {
    override fun hentNavIdentForInnloggetBruker(): String = "Z123456"
}
