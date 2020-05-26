package no.nav.sbl.sosialhjelpmodiaapi.idporten

import com.fasterxml.jackson.module.kotlin.readValue
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.util.Base64
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.sbl.sosialhjelpmodiaapi.common.retry
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.io.File
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Profile("!mock")
@Component
class IdPortenService(
        clientProperties: ClientProperties,
        private val restTemplate: RestTemplate
) {

    private val idPortenTokenUrl = clientProperties.idPortenTokenUrl
    private val idPortenClientId = clientProperties.idPortenClientId
    private val idPortenScope = clientProperties.idPortenScope
    private val idPortenConfigUrl = clientProperties.idPortenConfigUrl
    private val idPortenDefaultIssuer = clientProperties.idPortenDefaultIssuer
    private val VIRKSERT_STI: String? = System.getenv("VIRKSERT_STI") ?: "/var/run/secrets/nais.io/virksomhetssertifikat"

    private var oidcConfigurationTimeStamp: Long = -1
    private var oidcConfiguration: IdPortenOidcConfiguration = IdPortenOidcConfiguration(idPortenDefaultIssuer, idPortenTokenUrl)

    suspend fun requestToken(attempts: Int = 10): AccessToken =
            retry(attempts = attempts, retryableExceptions = *arrayOf(HttpServerErrorException::class)) {
                val jws = createJws()
                log.info("Got jws, getting token")
                val uriComponents = UriComponentsBuilder.fromHttpUrl(idPortenTokenUrl).build()
                val body = LinkedMultiValueMap<String, String>()
                body.add(GRANT_TYPE_PARAM, GRANT_TYPE).toString()
                body.add(ASSERTION_PARAM, jws.token).toString()
                val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.POST, HttpEntity(body, HttpHeaders()), String::class.java)
                val returnObject : IdPortenAccessTokenResponse = objectMapper.readValue(response.body!!)
                AccessToken(returnObject.accessToken)
            }

    fun createJws(
            expirySeconds: Int = 100,
            issuer: String = idPortenClientId,
            scope: String = idPortenScope
    ): Jws {
        require(expirySeconds <= MAX_EXPIRY_SECONDS) {
            "IdPorten: JWT expiry cannot be greater than $MAX_EXPIRY_SECONDS seconds (was $expirySeconds)"
        }


        val date = Date()
        val expDate: Date = Calendar.getInstance().let {
            it.time = date
            it.add(Calendar.SECOND, expirySeconds)
            it.time
        }
        val virksertCredentials = objectMapper.readValue<VirksertCredentials>(
                File("$VIRKSERT_STI/credentials.json").readText(Charsets.UTF_8)
        )

        val pair = KeyStore.getInstance("PKCS12").let { keyStore ->
            keyStore.load(
                    java.util.Base64.getDecoder().decode(File("$VIRKSERT_STI/key.p12.b64").readText(Charsets.UTF_8)).inputStream(),
                    virksertCredentials.password.toCharArray()
            )
            val cert = keyStore.getCertificate(virksertCredentials.alias) as X509Certificate

            KeyPair(
                    cert.publicKey,
                    keyStore.getKey(
                            virksertCredentials.alias,
                            virksertCredentials.password.toCharArray()
                    ) as PrivateKey
            ) to cert.encoded
        }

        if(oidcConfigurationTimeStamp == -1L) {
            try {
                val uriComponents = UriComponentsBuilder.fromHttpUrl(idPortenConfigUrl).build()
                val response = restTemplate.exchange(uriComponents.toUriString(), HttpMethod.GET, HttpEntity<Nothing>(HttpHeaders()), String::class.java)
                val returnObject: IdPortenOidcConfiguration = objectMapper.readValue(response.body!!)
                log.info("Hentet config fra $idPortenConfigUrl")
                oidcConfiguration = returnObject
                oidcConfigurationTimeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            } catch (e:Exception) {
                log.error("Feil har oppstått når vi henter IdPorten konfiguration fra: $idPortenConfigUrl", e)
            }
        }

        log.info("Public certificate length " + pair.first.public.encoded.size)

        return SignedJWT(
                JWSHeader.Builder(JWSAlgorithm.RS256).x509CertChain(mutableListOf(Base64.encode(pair.second))).build(),
                JWTClaimsSet.Builder()
                        .audience(oidcConfiguration.issuer)
                        .issuer(issuer)
                        .issueTime(date)
                        .jwtID(UUID.randomUUID().toString())
                        .expirationTime(expDate)
                        .claim(CLAIMS_SCOPE, scope)
                        .build()
        ).run {
            sign(RSASSASigner(pair.first.private))
            val jws = Jws(serialize())
            log.info("Serialized JWS")
            jws
        }
    }

    companion object {
        private const val MAX_EXPIRY_SECONDS = 120
        private const val CLAIMS_SCOPE = "scope"
        private const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer"

        private const val GRANT_TYPE_PARAM = "grant_type"
        private const val ASSERTION_PARAM = "assertion"

        private val log by logger()
    }

    private data class VirksertCredentials(
            val alias: String,
            val password: String,
            val type: String
    )

}
