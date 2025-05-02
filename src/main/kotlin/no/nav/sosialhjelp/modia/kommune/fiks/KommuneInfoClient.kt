package no.nav.sosialhjelp.modia.kommune.fiks

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.api.fiks.exceptions.FiksClientException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksServerException
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.app.maskinporten.MaskinportenClient
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.typeRef
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_INTEGRASJON_ID
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_INTEGRASJON_PASSORD
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KommuneInfoClient(
    private val maskinportenClient: MaskinportenClient,
    private val clientProperties: ClientProperties,
    private val fiksWebClient: WebClient,
) {
    fun getKommuneInfo(kommunenummer: String): KommuneInfo =
        fiksWebClient
            .get()
            .uri(PATH_KOMMUNEINFO, kommunenummer)
            .accept(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .header(HEADER_INTEGRASJON_ID, clientProperties.fiksIntegrasjonId)
            .header(HEADER_INTEGRASJON_PASSORD, clientProperties.fiksIntegrasjonpassord)
            .retrieve()
            .bodyToMono<KommuneInfo>()
            .onErrorMap(WebClientResponseException::class.java) { e ->
                log.warn("Fiks - hentKommuneInfoForAlle feilet", e)
                when {
                    e.statusCode.is4xxClientError -> FiksClientException(e.statusCode.value(), e.message, e)
                    else -> FiksServerException(e.statusCode.value(), e.message, e)
                }
            }.block()
            ?: throw RuntimeException("Noe feil skjedde ved henting av KommuneInfo for kommune=$kommunenummer")

    fun getAll(): List<KommuneInfo> =
        fiksWebClient
            .get()
            .uri(PATH_ALLE_KOMMUNEINFO)
            .accept(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .header(HEADER_INTEGRASJON_ID, clientProperties.fiksIntegrasjonId)
            .header(HEADER_INTEGRASJON_PASSORD, clientProperties.fiksIntegrasjonpassord)
            .retrieve()
            .bodyToMono(typeRef<List<KommuneInfo>>())
            .onErrorMap(WebClientResponseException::class.java) { e ->
                log.warn("Fiks - hentKommuneInfoForAlle feilet", e)
                when {
                    e.statusCode.is4xxClientError -> FiksClientException(e.statusCode.value(), e.message, e)
                    else -> FiksServerException(e.statusCode.value(), e.message, e)
                }
            }.block()
            ?: emptyList()

    companion object {
        private val log by logger()

        const val PATH_KOMMUNEINFO = "/digisos/api/v1/nav/kommuner/{kommunenummer}"
        const val PATH_ALLE_KOMMUNEINFO = "/digisos/api/v1/nav/kommuner"
    }
}
