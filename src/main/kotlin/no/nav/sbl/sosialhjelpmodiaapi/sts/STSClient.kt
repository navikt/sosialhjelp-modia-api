package no.nav.sbl.sosialhjelpmodiaapi.sts

import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.sts.STSToken.Companion.shouldRenewToken
import no.nav.sbl.sosialhjelpmodiaapi.typeRef
import no.nav.sbl.sosialhjelpmodiaapi.utils.basicHeader
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.lang.System.getenv
import java.time.LocalDateTime

@Component
class STSClient(private val restTemplate: RestTemplate,
                private val clientProperties: ClientProperties) {

    companion object {
        val log by logger()

        const val SRVSOSIALHJELP_MODIA_API_USERNAME: String = "SRVSOSIALHJELP_MODIA_API_USERNAME"
        const val SRVSOSIALHJELP_MODIA_API_PASSWORD: String = "SRVSOSIALHJELP_MODIA_API_PASSWORD"
    }

    private var cachedToken: STSToken? = null

    fun token(): String {
        if (shouldRenewToken(cachedToken)) {
            val requestEntity = HttpEntity<Any>(basicAuthHeader())
            val requestUrl = clientProperties.stsTokenEndpointUrl

            try {
                val response = restTemplate.exchange(requestUrl, GET, requestEntity, typeRef<STSToken>())

                cachedToken = response.body

            } catch (e: HttpClientErrorException) {
                log.error("STS - ${e.statusCode} ${e.statusText}", e)
                throw e
            }
        }
        return cachedToken!!.access_token
    }

    private fun basicAuthHeader(): HttpHeaders {
        val srvusername: String? = getenv(SRVSOSIALHJELP_MODIA_API_USERNAME)
        val srvpassword: String? = getenv(SRVSOSIALHJELP_MODIA_API_PASSWORD)
        if (srvusername == null || srvpassword == null) {
            throw IllegalStateException("srvusername eller srvpassword er ikke tilgjengelig")
        }

        val basicAuthHeader = basicHeader(srvusername, srvpassword)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, basicAuthHeader)
        return headers
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
            if (token == null){
                return true
            }
            return isExpired(token)
        }

        private fun isExpired(token: STSToken): Boolean {
            return token.expirationTime.isBefore(LocalDateTime.now())
        }
    }
}