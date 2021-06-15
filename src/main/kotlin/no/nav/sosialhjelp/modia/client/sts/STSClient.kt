package no.nav.sosialhjelp.modia.client.sts

import no.nav.sosialhjelp.modia.client.sts.STSToken.Companion.shouldRenewToken
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.logger
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.OPTIONS
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime

@Profile("!(mock | local)")
@Component
class STSClient(
    private val serviceuserBasicAuthRestTemplate: RestTemplate,
    clientProperties: ClientProperties
) {

    private val tokenEndpointUrl = clientProperties.stsTokenEndpointUrl

    private var cachedToken: STSToken? = null

    fun token(): String {
        if (shouldRenewToken(cachedToken)) {
            try {
                log.info("Henter nytt token fra STS")
                val response = serviceuserBasicAuthRestTemplate.exchange(tokenEndpointUrl, POST, requestEntity(), STSToken::class.java)

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

    fun ping() {
        try {
            serviceuserBasicAuthRestTemplate.exchange(tokenEndpointUrl, OPTIONS, null, String::class.java)
        } catch (e: RestClientException) {
            log.warn("Selftest - STS - ping feilet", e)
            throw e
        }
    }

    private fun requestEntity(): HttpEntity<MultiValueMap<String, String>> {
        val headers = HttpHeaders()
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)

        val map = LinkedMultiValueMap<String, String>()
        map.add(GRANT_TYPE, CLIENT_CREDENTIALS)
        map.add(SCOPE, OPENID)

        return HttpEntity(map, headers)
    }

    companion object {
        private val log by logger()

        private const val GRANT_TYPE = "grant_type"
        private const val CLIENT_CREDENTIALS = "client_credentials"
        private const val SCOPE = "scope"
        private const val OPENID = "openid"
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
