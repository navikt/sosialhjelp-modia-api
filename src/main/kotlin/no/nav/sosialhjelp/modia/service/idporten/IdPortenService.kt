package no.nav.sosialhjelp.modia.service.idporten

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.modia.service.idporten.IdPortenServiceImpl.CachedToken.Companion.shouldRenewToken
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.forwardHeaders
import no.nav.sosialhjelp.idporten.client.AccessToken
import no.nav.sosialhjelp.idporten.client.IdPortenClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime

interface IdPortenService {

    fun getToken(): AccessToken

}

@Component
@Profile("!mock")
class IdPortenServiceImpl(
        private val idPortenClient: IdPortenClient
) : IdPortenService {

    private var cachedToken: CachedToken? = null

    override fun getToken(): AccessToken {
        if (shouldRenewToken(cachedToken)) {
            val tidspunktForHenting: LocalDateTime = LocalDateTime.now()
            return runBlocking(Dispatchers.IO) { idPortenClient.requestToken(headers = forwardHeaders()) }
                    .also { cachedToken = CachedToken(it, tidspunktForHenting) }
        }

        return cachedToken!!.accessToken
    }

    private data class CachedToken(
            val accessToken: AccessToken,
            val created: LocalDateTime
    ) {
        // 10 sek buffer fra expiresIn
        val expirationTime: LocalDateTime = LocalDateTime.now().plusSeconds(accessToken.expiresIn - 10L)

        companion object {
            fun shouldRenewToken(token: CachedToken?): Boolean {
                if (token == null) {
                    return true
                }
                return isExpired(token)
            }

            private fun isExpired(token: CachedToken): Boolean {
                return token.expirationTime.isBefore(LocalDateTime.now())
            }
        }
    }
}
