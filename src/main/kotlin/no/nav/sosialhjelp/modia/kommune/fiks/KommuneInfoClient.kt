package no.nav.sosialhjelp.modia.kommune.fiks

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.api.fiks.exceptions.FiksClientException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksServerException
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.auth.texas.TexasClient
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_INTEGRASJON_ID
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_INTEGRASJON_PASSORD
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient

@Component
class KommuneInfoClient(
    private val clientProperties: ClientProperties,
    private val fiksRestClient: RestClient,
    private val texasClient: TexasClient,
) {
    fun getKommuneInfo(kommunenummer: String): KommuneInfo = try {
        fiksRestClient
            .get()
            .uri(PATH_KOMMUNEINFO, kommunenummer)
            .accept(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + texasClient.getMaskinportenToken())
            .header(HEADER_INTEGRASJON_ID, clientProperties.fiksIntegrasjonId)
            .header(HEADER_INTEGRASJON_PASSORD, clientProperties.fiksIntegrasjonpassord)
            .retrieve()
            .body(KommuneInfo::class.java)
            ?: throw RuntimeException("Noe feil skjedde ved henting av KommuneInfo for kommune=$kommunenummer")
    } catch (e: HttpClientErrorException) {
        log.warn("Fiks - hentKommuneInfoForAlle feilet", e)
        throw FiksClientException(e.statusCode.value(), e.message, e)
    } catch (e: HttpServerErrorException) {
        log.warn("Fiks - hentKommuneInfoForAlle feilet", e)
        throw FiksServerException(e.statusCode.value(), e.message, e)
    }

    companion object {
        private val log by logger()

        const val PATH_KOMMUNEINFO = "/digisos/api/v1/nav/kommuner/{kommunenummer}"
        const val PATH_ALLE_KOMMUNEINFO = "/digisos/api/v1/nav/kommuner"
    }
}
