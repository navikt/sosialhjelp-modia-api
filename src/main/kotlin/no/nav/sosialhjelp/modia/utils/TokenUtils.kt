package no.nav.sosialhjelp.modia.utils

import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.sosialhjelp.modia.app.msgraph.MsGraphClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

interface TokenUtils {
    fun hentNavIdentForInnloggetBruker(): String

    fun hentTokenMedGraphScope(): String
}

@Profile("!(mock-alt | local)")
@Component
class TokenUtilsImpl(
    private val clientConfigurationProperties: ClientConfigurationProperties,
    private val oAuth2AccessTokenService: OAuth2AccessTokenService,
    private val msGraphClient: MsGraphClient,
) : TokenUtils {
    override fun hentTokenMedGraphScope(): String =
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

    override fun hentTokenMedGraphScope(): String = "msgraph-token"
}
