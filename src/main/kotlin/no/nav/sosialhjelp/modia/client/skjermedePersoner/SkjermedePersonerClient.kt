package no.nav.sosialhjelp.modia.client.skjermedePersoner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.modia.client.azure.AzureAppTokenUtils
import no.nav.sosialhjelp.modia.client.azure.buildWebClient
import no.nav.sosialhjelp.modia.client.skjermedePersoner.model.SkjermedePersonerRequest
import no.nav.sosialhjelp.modia.common.ManglendeTilgangException
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import no.nav.sosialhjelp.modia.utils.objectMapper
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

interface SkjermedePersonerClient {
    fun erPersonSkjermet(ident: String): Boolean
}

@Profile("!test")
@Component
class SkjermedePersonerClientImpl(
    webClientBuilder: WebClient.Builder,
    private val azureAppTokenUtils: AzureAppTokenUtils,
    private val redisService: RedisService,
    private val clientProperties: ClientProperties,
) : SkjermedePersonerClient {

    private val webClient: WebClient = buildWebClient(webClientBuilder, clientProperties.skjermedePersonerEndpointUrl)

    override fun erPersonSkjermet(ident: String): Boolean {
        hentFraCache(ident)?.let { return it }
        return hentSkjermetStatusFraServer(ident).also { lagreSkjermetStatus(it, ident) }
    }

    private fun hentFraCache(ident: String): Boolean? {
        val skjermetStatus = redisService.get("SKJERMEDE_PERSONER_$ident", Boolean::class.java)
        return skjermetStatus?.let { return it as Boolean }
    }

    private fun lagreSkjermetStatus(skjermet: Boolean?, ident: String) {
        skjermet?.let { redisService.set("SKJERMEDE_PERSONER_$ident", objectMapper.writeValueAsBytes(it), 2 * 60 * 60) }
    }

    private fun hentSkjermetStatusFraServer(ident: String): Boolean {
        log.debug("Sjekker om person er skjermet.")
        val azureAdToken = azureAppTokenUtils.hentTokenMedSkjermedePersonerScope()

        val response: String = runBlocking(Dispatchers.IO) {
            webClient.post()
                .uri("/skjermet")
                .header(HttpHeaders.AUTHORIZATION, BEARER + azureAdToken)
                .bodyValue(SkjermedePersonerRequest(ident))
                .retrieve()
                .bodyToMono<String>()
                .onErrorMap { e ->
                    when (e) {
                        is WebClientResponseException ->
                            log.error(
                                "Skjermede personer - noe feilet. Status: ${e.statusCode}, message: ${e.message}." +
                                    e.responseBodyAsString,
                                e
                            )
                        is WebClientRequestException -> log.error(
                            "Skjermede personer - oppkobling feilet. Message: ${e.message}.",
                            e
                        )
                        else -> log.error("Skjermede personer - noe feilet.", e)
                    }
                    ManglendeTilgangException("Noe feilet ved kall til Skjermede personer: ${e.message}")
                }
                .awaitSingle()
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

    override fun erPersonSkjermet(ident: String): Boolean {
        return false
    }
}
