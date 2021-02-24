package no.nav.sosialhjelp.modia.utils

import no.nav.sosialhjelp.modia.client.msgraph.MsGraphClient
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

interface TokenUtils {
    fun hentNavIdentForInnloggetBruker(): String
    fun hentTokenMedGraphScope(): String
}

@Profile("!(mock | mock-alt | local)")
@Component
class TokenUtilsImpl(
        private val clientConfigurationProperties: ClientConfigurationProperties,
        private val oAuth2AccessTokenService: OAuth2AccessTokenService,
        private val msGraphClient: MsGraphClient
) : TokenUtils {

    override fun hentTokenMedGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["onbehalfof"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    override fun hentNavIdentForInnloggetBruker(): String {
        return msGraphClient.hentOnPremisesSamAccountName(hentTokenMedGraphScope()).onPremisesSamAccountName
    }
}

@Profile("(mock | mock-alt | local)")
@Component
class MockTokenUtils : TokenUtils {

    override fun hentNavIdentForInnloggetBruker(): String {
        return "Z123456"
    }

    override fun hentTokenMedGraphScope(): String {
        return "msgraph-token"
    }
}