package no.nav.sosialhjelp.modia.client.digisosapi

import no.nav.sosialhjelp.api.fiks.exceptions.FiksClientException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksServerException
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.service.idporten.IdPortenService
import no.nav.sosialhjelp.modia.utils.DigisosApiWrapper
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_INTEGRASJON_ID
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_INTEGRASJON_PASSORD
import no.nav.sosialhjelp.modia.utils.Miljo.getTestbrukerNatalie
import no.nav.sosialhjelp.modia.utils.objectMapper
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.Collections

@Profile("!(prod-fss|mock)")
@Component
class DigisosApiClientImpl(
    private val clientProperties: ClientProperties,
    private val fiksWebClient: WebClient,
    private val idPortenService: IdPortenService
) : DigisosApiClient {

    private val testbrukerNatalie = getTestbrukerNatalie()

    override fun oppdaterDigisosSak(fiksDigisosId: String?, digisosApiWrapper: DigisosApiWrapper): String? {
        var id = fiksDigisosId
        if (fiksDigisosId == null || fiksDigisosId == "001" || fiksDigisosId == "002" || fiksDigisosId == "003") {
            id = opprettDigisosSak()
            log.info("Laget ny digisossak: $id")
        }
        return fiksWebClient.post()
            .uri("/digisos/api/v1/11415cd1-e26d-499a-8421-751457dfcbd5/$id")
            .headers { it.addAll(headers()) }
            .body(BodyInserters.fromValue(objectMapper.writeValueAsString(digisosApiWrapper)))
            .retrieve()
            .bodyToMono<String>()
            .onErrorMap(WebClientResponseException::class.java) { e ->
                log.warn("Fiks - oppdaterDigisosSak feilet - ${e.statusCode} ${e.statusText}", e)
                when {
                    e.statusCode.is4xxClientError -> FiksClientException(e.rawStatusCode, e.message, e)
                    else -> FiksServerException(e.rawStatusCode, e.message, e)
                }
            }
            .block()
            .also { log.info("Postet DigisosSak til Fiks") }
    }

    fun opprettDigisosSak(): String? {
        return fiksWebClient.post()
            .uri("/digisos/api/v1/11415cd1-e26d-499a-8421-751457dfcbd5/ny?sokerFnr=$testbrukerNatalie")
            .headers { it.addAll(headers()) }
            .body(BodyInserters.fromValue(""))
            .retrieve()
            .bodyToMono<String>()
            .onErrorMap(WebClientResponseException::class.java) { e ->
                log.warn("Fiks - opprettDigisosSak feilet - ${e.statusCode} ${e.statusText}", e)
                when {
                    e.statusCode.is4xxClientError -> FiksClientException(e.rawStatusCode, e.message, e)
                    else -> FiksServerException(e.rawStatusCode, e.message, e)
                }
            }
            .block()
            ?.replace("\"", "")
            .also { log.info("Opprettet sak hos Fiks. Digisosid: $it") }
    }

    private fun headers(): HttpHeaders {
        val headers = HttpHeaders()
        val accessToken = idPortenService.getToken()
        headers.accept = Collections.singletonList(MediaType.ALL)
        headers.set(HEADER_INTEGRASJON_ID, clientProperties.fiksIntegrasjonIdKommune)
        headers.set(HEADER_INTEGRASJON_PASSORD, clientProperties.fiksIntegrasjonPassordKommune)
        headers.set(AUTHORIZATION, BEARER + accessToken.token)
        headers.contentType = MediaType.APPLICATION_JSON
        return headers
    }

    companion object {
        private val log by logger()
    }
}
