package no.nav.sosialhjelp.modia.tilgang.azure

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.redis.RedisKeyType
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.utils.MiljoUtils
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Instant
import java.util.Date
import java.util.UUID

@Service
class AzuredingsService(
    private val azuredingsClient: AzuredingsClient,
    private val redisService: RedisService,
    private val clientProperties: ClientProperties,
    miljoUtils: MiljoUtils,
) {

    private val privateRsaKey: RSAKey = if (clientProperties.azuredingsPrivateJwk == "generateRSA") {
        if (miljoUtils.isRunningInProd()) throw RuntimeException("Generation of RSA keys is not allowed in prod.")
        RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE).keyID(UUID.randomUUID().toString()).generate()
    } else {
        RSAKey.parse(clientProperties.azuredingsPrivateJwk)
    }

    suspend fun exchangeToken(token: String, scope: String): String {
        val redisKey = "$token$scope"
        redisService.getString(RedisKeyType.AZUREDINGS, redisKey)?.let { return it }

        val jwt = createSignedAssertion(clientProperties.azuredingsJwtClientId, clientProperties.azuredingsJwtAudience, privateRsaKey)

        return try {
            azuredingsClient.exchangeToken(token, jwt, clientProperties.azuredingsJwtClientId, scope).accessToken
                .also { lagreTilCache(redisKey, it) }
        } catch (e: WebClientResponseException) {
            log.warn("Error message from server: ${e.responseBodyAsString}")
            throw e
        }
    }

    private fun createSignedAssertion(clientId: String, audience: String, rsaKey: RSAKey): String {
        val now = Instant.now()
        return JWT.create()
            .withSubject(clientId)
            .withIssuer(clientId)
            .withAudience(audience)
            .withIssuedAt(Date.from(now))
            .withNotBefore(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(60)))
            .withJWTId(UUID.randomUUID().toString())
            .withKeyId(rsaKey.keyID)
            .sign(Algorithm.RSA256(null, rsaKey.toRSAPrivateKey()))
    }

    private fun lagreTilCache(key: String, onBehalfToken: String) {
        redisService.set(RedisKeyType.AZUREDINGS, key, onBehalfToken.toByteArray(), 30)
    }

    companion object {
        private val log by logger()
    }
}
