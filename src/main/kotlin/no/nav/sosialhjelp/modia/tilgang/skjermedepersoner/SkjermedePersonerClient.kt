package no.nav.sosialhjelp.modia.tilgang.skjermedepersoner

import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.app.exceptions.ManglendeTilgangException
import no.nav.sosialhjelp.modia.auth.texas.IdentityProvider
import no.nav.sosialhjelp.modia.auth.texas.TexasClient
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.redis.RedisKeyType.SKJERMEDE_PERSONER
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.tilgang.skjermedepersoner.model.SkjermedePersonerRequest
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import no.nav.sosialhjelp.modia.utils.sosialhjelpJsonMapper
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body

interface SkjermedePersonerClient {
    fun erPersonSkjermet(
        ident: String,
        veilederToken: String,
    ): Boolean
}

@Profile("!test")
@Component
class SkjermedePersonerClientImpl(
    private val redisService: RedisService,
    private val clientProperties: ClientProperties,
    private val texasClient: TexasClient,
) : SkjermedePersonerClient {
    private val skjermedePersonerRestClient = RestClient.builder().build()

    override fun erPersonSkjermet(
        ident: String,
        veilederToken: String,
    ): Boolean {
        hentFraCache(ident)?.let { return it }
        return hentSkjermetStatusFraServer(ident, veilederToken).also { lagreSkjermetStatus(it, ident) }
    }

    private fun hentFraCache(ident: String): Boolean? {
        val skjermetStatus = redisService.get(SKJERMEDE_PERSONER, ident, Boolean::class.java)
        return skjermetStatus?.let { return it }
    }

    private fun lagreSkjermetStatus(
        skjermet: Boolean?,
        ident: String,
    ) {
        skjermet?.let {
            redisService.set(SKJERMEDE_PERSONER, ident, sosialhjelpJsonMapper.writeValueAsBytes(it), 2 * 60 * 60)
        }
    }

    private fun hentSkjermetStatusFraServer(
        ident: String,
        veilederToken: String,
    ): Boolean {
        log.debug("Sjekker om person er skjermet.")
        val azureAdToken =
            texasClient.getTokenXToken(
                clientProperties.skjermedePersonerScope,
                veilederToken,
                IdentityProvider.ENTRA_ID,
            )

        val response: String =
            try {
                skjermedePersonerRestClient
                    .post()
                    .uri("${clientProperties.skjermedePersonerEndpointUrl}/skjermet")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + azureAdToken)
                    .body(SkjermedePersonerRequest(ident))
                    .retrieve()
                    .body<String>()
                    ?: throw ManglendeTilgangException("Skjermede personer - tom respons")
            } catch (e: RestClientResponseException) {
                log.error(
                    "Skjermede personer - noe feilet. Status: ${e.statusCode}, message: ${e.message}.\n ${e.responseBodyAsString}",
                    e,
                )
                throw ManglendeTilgangException("Noe feilet ved kall til Skjermede personer: ${e.message}")
            } catch (e: Exception) {
                log.error("Skjermede personer - noe feilet.", e)
                throw ManglendeTilgangException("Noe feilet ved kall til Skjermede personer: ${e.message}")
            }

        log.debug("Person er skjermet = $response")
        return "false" != response
    }

    companion object {
        private val log by logger()
    }
}

@Profile("test")
@Component
class SkjermedePersonerClientMock : SkjermedePersonerClient {
    override fun erPersonSkjermet(
        ident: String,
        veilederToken: String,
    ): Boolean = false
}
