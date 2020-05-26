package no.nav.sbl.sosialhjelpmodiaapi.client.sts

import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.client.sts.STSToken.Companion.shouldRenewToken
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod.GET
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime

@Profile("!(mock | local)")
@Component
class STSClient(
        private val serviceuserBasicAuthRestTemplate: RestTemplate,
        clientProperties: ClientProperties
) {

    private val baseUrl = clientProperties.stsTokenEndpointUrl

    private var cachedToken: STSToken? = null

    fun token(): String {
        if (shouldRenewToken(cachedToken)) {
            try {
                log.info("Henter nytt token fra STS")
                val requestUrl = lagRequest(baseUrl)
                val response = serviceuserBasicAuthRestTemplate.exchange(requestUrl, GET, null, STSToken::class.java)

                cachedToken = response.body
                return response.body!!.access_token
            } catch (e: RestClientException) {
                log.error("STS - Noe feilet, message: ${e.message}", e)
                throw e
            }
        }
        log.info("Hentet token fra cache")
        return cachedToken!!.access_token
    }

    private fun lagRequest(baseurl: String): String {
        return "$baseurl/token?grant_type=client_credentials&scope=openid"
    }

    companion object {
        private val log by logger()
    }
}

data class STSToken(
        val access_token: String,
        val token_type: String,
        val expires_in: Long
) {

    val expirationTime: LocalDateTime = LocalDateTime.now().plusSeconds(expires_in - 10L)

    companion object {
        fun shouldRenewToken(token: STSToken?): Boolean {
            if (token == null) {
                return true
            }
            return isExpired(token)
        }

        private fun isExpired(token: STSToken): Boolean {
            return token.expirationTime.isBefore(LocalDateTime.now())
        }
    }
}
