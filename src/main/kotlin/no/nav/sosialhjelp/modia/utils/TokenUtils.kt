package no.nav.sosialhjelp.modia.utils

import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.sosialhjelp.modia.app.msgraph.MsGraphClient
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

@Profile("!(mock-alt | local)&!gcp")
@Component
class TokenUtilsImplFss(
    private val clientConfigurationProperties: ClientConfigurationProperties,
    private val oAuth2AccessTokenService: OAuth2AccessTokenService,
    private val msGraphClient: MsGraphClient,
) : TokenUtils {
    fun hentTokenMedGraphScope(): String =
        clientConfigurationProperties.registration["onbehalfof"]
            ?.let { clientProperties -> oAuth2AccessTokenService.getAccessToken(clientProperties).access_token }
            ?: error("ClientProperties er null")

    override fun hentNavIdentForInnloggetBruker(): String =
        msGraphClient.hentOnPremisesSamAccountName(hentTokenMedGraphScope()).onPremisesSamAccountName
}

@Profile("(mock-alt | local)")
@Component
class MockTokenUtils : TokenUtils {
    override fun hentNavIdentForInnloggetBruker(): String = "Z123456"
}
