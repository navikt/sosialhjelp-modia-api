package no.nav.sbl.sosialhjelpmodiaapi.service.kommune

import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.service.idporten.IdPortenService
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import org.springframework.stereotype.Component

@Component
class KommuneService(
        private val kommuneInfoClient: KommuneInfoClient,
        private val idPortenService: IdPortenService
) {

    fun get(kommunenummer: String): KommuneInfo {
        return kommuneInfoClient.get(kommunenummer, getToken())
    }

    fun getBehandlingsanvarligKommune(kommunenummer: String): String? {
        val behandlingsansvarlig = kommuneInfoClient.get(kommunenummer, getToken()).behandlingsansvarlig

        return if (behandlingsansvarlig != null) leggTilKommuneINavnet(behandlingsansvarlig) else null
    }

    private fun leggTilKommuneINavnet(kommunenavn: String): String {
       return if (kommunenavn.toLowerCase().endsWith(" kommune")) kommunenavn else "$kommunenavn kommune"
    }

    fun getAll(): List<KommuneInfo> {
        return kommuneInfoClient.getAll(getToken())
    }

    private fun getToken(): String {
        return idPortenService.getToken().token
    }

    companion object {
        private val log by logger()
    }
}
