package no.nav.sbl.sosialhjelpmodiaapi.service.idporten

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.sbl.sosialhjelpmodiaapi.service.idporten.IdPortenService.CachedToken.Companion.shouldRenewToken
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.forwardHeaders
import no.nav.sosialhjelp.idporten.client.AccessToken
import no.nav.sosialhjelp.idporten.client.IdPortenClient
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class IdPortenService(
        private val idPortenClient: IdPortenClient
) {

    private var cachedToken: CachedToken? = null

    fun getToken(): AccessToken {
        if (shouldRenewToken(cachedToken)) {
            val tidspunktForHenting: LocalDateTime = LocalDateTime.now()
            val headers = forwardHeaders()
            return runBlocking(Dispatchers.IO) { idPortenClient.requestToken(headers = headers) }
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
