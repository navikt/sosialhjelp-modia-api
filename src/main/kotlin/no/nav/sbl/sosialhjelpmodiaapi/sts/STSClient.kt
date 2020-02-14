package no.nav.sbl.sosialhjelpmodiaapi.sts

import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.sts.STSToken.Companion.shouldRenewToken
import no.nav.sbl.sosialhjelpmodiaapi.typeRefr
import org.springframework.http.HttpMethod.GET
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime

@Component
class STSClient(private val stsRestTemplate: RestTemplate,
                private val clientProperties: ClientProperties) {

    companion object {
        val log by logger()
    }

    private var cachedToken: STSToken? = null

    fun token(): String {
        if (shouldRenewToken(cachedToken)) {
            try {
                log.info("Henter nytt token fra STS")
                val requestUrl = clientProperties.stsTokenEndpointUrl
                val response = stsRestTemplate.exchange(requestUrl, GET, null, typeRef<STSToken>())

                cachedToken = response.body
                return response.body!!.access_token
            } catch (e: HttpClientErrorException) {
                log.error("STS - ${e.statusCode} ${e.statusText}", e)
                throw e
            }
        }
        log.info("Hentet token fra cache")
        return cachedToken!!.access_token
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