package no.nav.sosialhjelp.modia.tilgang.skjermedepersoner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.app.exceptions.ManglendeTilgangException
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.redis.RedisKeyType.SKJERMEDE_PERSONER
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.tilgang.azure.AzuredingsService
import no.nav.sosialhjelp.modia.tilgang.skjermedepersoner.model.SkjermedePersonerRequest
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import no.nav.sosialhjelp.modia.utils.configureWebClient
import no.nav.sosialhjelp.modia.utils.sosialhjelpJsonMapper
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

interface SkjermedePersonerClient {
    fun erPersonSkjermet(
        ident: String,
        veilederToken: String,
    ): Boolean
}

@Profile("!test")
@Component
class SkjermedePersonerClientImpl(
    webClientBuilder: WebClient.Builder,
    private val azuredingsService: AzuredingsService,
    private val redisService: RedisService,
    private val clientProperties: ClientProperties,
) : SkjermedePersonerClient {
    private val skjermedePersonerWebClient: WebClient = webClientBuilder.configureWebClient()

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

        val response: String =
            runBlocking(Dispatchers.IO) {
                val azureAdToken = azuredingsService.exchangeToken(veilederToken, clientProperties.skjermedePersonerScope)
                skjermedePersonerWebClient
                    .post()
                    .uri("${clientProperties.skjermedePersonerEndpointUrl}/skjermet")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + azureAdToken)
                    .bodyValue(SkjermedePersonerRequest(ident))
                    .retrieve()
                    .bodyToMono<String>()
                    .onErrorMap { e ->
                        when (e) {
                            is WebClientResponseException ->
                                log.error(
                                    "Skjermede personer - noe feilet. Status: ${e.statusCode}, message: ${e.message}.\n ${e.responseBodyAsString}",
                                    e,
                                )
                            is WebClientRequestException ->
                                log.error(
                                    "Skjermede personer - oppkobling feilet. Message: ${e.message}.",
                                    e,
                                )
                            else -> log.error("Skjermede personer - noe feilet.", e)
                        }
                        ManglendeTilgangException("Noe feilet ved kall til Skjermede personer: ${e.message}")
                    }.awaitSingle()
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
