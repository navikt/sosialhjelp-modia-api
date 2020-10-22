package no.nav.sbl.sosialhjelpmodiaapi.utils

import no.nav.sbl.sosialhjelpmodiaapi.client.msgraph.MsGraphClient
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

interface TokenUtils {

    fun getInnloggetNavIdent(): String
}

@Profile("!(mock | mock-alt | local)")
@Component
class TokenUtilsImpl(
        private val clientConfigurationProperties: ClientConfigurationProperties,
        private val oAuth2AccessTokenService: OAuth2AccessTokenService,
        private val msGraphClient: MsGraphClient
) : TokenUtils {

    private fun getTokenWithGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["onbehalfof"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    override fun getInnloggetNavIdent(): String {
        return msGraphClient.hentOnPremisesSamAccountName(getTokenWithGraphScope()).onPremisesSamAccountName
    }
}

@Profile("(mock | mock-alt | local)")
@Component
class MockTokenUtils : TokenUtils {

    override fun getInnloggetNavIdent(): String {
        return "Z123456"
    }
}