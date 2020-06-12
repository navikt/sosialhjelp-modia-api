package no.nav.sbl.sosialhjelpmodiaapi.client.fiks

import kotlinx.coroutines.runBlocking
import no.nav.sbl.sosialhjelpmodiaapi.client.idporten.IdPortenService
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksException
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.kommuneinfo.FiksProperties
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

interface HentKommuneInfo {
    fun hentKommuneInfo(kommunenummer: String): KommuneInfo
    fun hentAlleKommuneInfo(): List<KommuneInfo>
}

@Profile("!mock")
@Component
class HentKommuneInfoClient(
        restTemplate: RestTemplate,
        private val clientProperties: ClientProperties,
        private val idPortenService: IdPortenService
) : HentKommuneInfo {

    private val fiksProperties = FiksProperties(
            hentKommuneInfoUrl = clientProperties.fiksDigisosEndpointUrl + FiksPaths.PATH_KOMMUNEINFO,
            hentAlleKommuneInfoUrl = clientProperties.fiksDigisosEndpointUrl + FiksPaths.PATH_ALLE_KOMMUNEINFO
    )

    private val kommuneInfoClient = KommuneInfoClient(restTemplate, fiksProperties)

    override fun hentKommuneInfo(kommunenummer: String): KommuneInfo {
        try {
            val headers = IntegrationUtils.fiksHeaders(clientProperties, getToken())
            return kommuneInfoClient.hentKommuneInfo(kommunenummer, headers)
        } catch (t: Throwable) {
            log.warn("Fiks - hentKommuneInfo feilet - ${t.message}")
            throw FiksException(null, t.message, t)
        }
    }

    override fun hentAlleKommuneInfo(): List<KommuneInfo> {
        try {
            val headers = IntegrationUtils.fiksHeaders(clientProperties, getToken())
            return kommuneInfoClient.hentAlleKommuneInfo(headers)
        } catch (t: Throwable) {
            log.warn("Fiks - hentAlleKommuneInfo feilet - ${t.message}", t)
            throw FiksException(null, t.message, t)
        }
    }

    private fun getToken(): String {
        val virksomhetstoken = runBlocking { idPortenService.requestToken() }
        return IntegrationUtils.BEARER + virksomhetstoken.token
    }

    companion object {
        private val log by logger()
    }
}