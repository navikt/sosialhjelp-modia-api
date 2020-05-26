package no.nav.sbl.sosialhjelpmodiaapi.client.digisosapi

import kotlinx.coroutines.runBlocking
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksException
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.client.idporten.IdPortenService
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.utils.DigisosApiWrapper
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.BEARER
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.HEADER_INTEGRASJON_ID
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.HEADER_INTEGRASJON_PASSORD
import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.util.*


@Profile("!mock")
@Component
class DigisosApiClientImpl(
        clientProperties: ClientProperties,
        private val restTemplate: RestTemplate,
        private val idPortenService: IdPortenService
) : DigisosApiClient {

    private val baseUrl = clientProperties.fiksDigisosEndpointUrl
    private val fiksIntegrasjonIdKommune = clientProperties.fiksIntegrasjonIdKommune
    private val fiksIntegrasjonPassordKommune = clientProperties.fiksIntegrasjonPassordKommune

    override fun oppdaterDigisosSak(fiksDigisosId: String?, digisosApiWrapper: DigisosApiWrapper): String? {
        var id = fiksDigisosId
        if (fiksDigisosId == null || fiksDigisosId == "001" || fiksDigisosId == "002" || fiksDigisosId == "003") {
            id = opprettDigisosSak()
            log.info("Laget ny digisossak: $id")
        }
        val httpEntity = HttpEntity(objectMapper.writeValueAsString(digisosApiWrapper), headers())
        try {
            restTemplate.exchange("$baseUrl/digisos/api/v1/11415cd1-e26d-499a-8421-751457dfcbd5/$id", HttpMethod.POST, httpEntity, String::class.java)
            log.info("Postet DigisosSak til Fiks")
            return id
        } catch (e: HttpStatusCodeException) {
            log.warn("Fiks - oppdaterDigisosSak feilet - ${e.statusCode} ${e.statusText}", e)
            throw FiksException(e.statusCode, e.message, e)
        } catch (e: Exception) {
            log.error(e.message, e)
            throw FiksException(null, e.message, e)
        }
    }

    fun opprettDigisosSak(): String? {
        val httpEntity = HttpEntity("", headers())
        try {
            val response = restTemplate.exchange("$baseUrl/digisos/api/v1/11415cd1-e26d-499a-8421-751457dfcbd5/ny?sokerFnr=26104500284", HttpMethod.POST, httpEntity, String::class.java)
            log.info("Opprettet sak hos Fiks. Digisosid: ${response.body}")
            return response.body?.replace("\"", "")
        } catch (e: HttpStatusCodeException) {
            log.warn("Fiks - opprettDigisosSak feilet - ${e.statusCode} ${e.statusText}", e)
            throw FiksException(e.statusCode, e.message, e)
        } catch (e: Exception) {
            log.error(e.message, e)
            throw FiksException(null, e.message, e)
        }
    }

    private fun headers(): HttpHeaders {
        val headers = HttpHeaders()
        val accessToken = runBlocking { idPortenService.requestToken() }
        headers.accept = Collections.singletonList(MediaType.ALL)
        headers.set(HEADER_INTEGRASJON_ID, fiksIntegrasjonIdKommune)
        headers.set(HEADER_INTEGRASJON_PASSORD, fiksIntegrasjonPassordKommune)
        headers.set(AUTHORIZATION, BEARER + accessToken.token)
        headers.contentType = MediaType.APPLICATION_JSON
        return headers
    }

    companion object {
        private val log by logger()
    }
}
