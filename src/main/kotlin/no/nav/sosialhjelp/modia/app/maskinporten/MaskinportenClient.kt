package no.nav.sosialhjelp.modia.app.maskinporten

import com.nimbusds.jwt.SignedJWT
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.utils.MiljoUtils
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class MaskinportenClient(
    private val proxiedWebClient: WebClient,
    maskinportenProperties: MaskinportenProperties,
    private val wellKnown: WellKnown,
    private val miljoUtils: MiljoUtils
) {
    private var cachedToken: SignedJWT? = null

    private val tokenGenerator = MaskinportenGrantTokenGenerator(maskinportenProperties, wellKnown.issuer, miljoUtils)

    fun getToken(): String {
        return getTokenFraCache() ?: getTokenFraMaskinporten()
    }

    private fun getTokenFraCache(): String? {
        return cachedToken?.takeUnless { isExpired(it) }?.parsedString
    }

    private fun getTokenFraMaskinporten(): String {
        val response = proxiedWebClient.post()
            .uri(wellKnown.token_endpoint)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(params))
            .retrieve()
            .bodyToMono<MaskinportenResponse>()
            .doOnSuccess { log.info("Hentet token fra Maskinporten") }
            .doOnError { log.warn("Noe feilet ved henting av token fra Maskinporten", it) }
            .block() ?: throw RuntimeException("Noe feilet ved henting av token fra Maskinporten")

        return response.access_token
            .also { cachedToken = SignedJWT.parse(it) }
    }

    private val params: MultiValueMap<String, String>
        get() = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", GRANT_TYPE)
            add("assertion", tokenGenerator.getJwt())
        }

    companion object {
        private val log by logger()

        private const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer"
        private const val TJUE_SEKUNDER: Long = 20

        private fun isExpired(jwt: SignedJWT): Boolean {
            return jwt.jwtClaimsSet?.expirationTime
                ?.toLocalDateTime?.minusSeconds(TJUE_SEKUNDER)?.isBefore(LocalDateTime.now())
                ?: true
        }

        private val Date.toLocalDateTime: LocalDateTime?
            get() = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }
}
