package no.nav.sbl.sosialhjelpmodiaapi.service.kommune

import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import org.springframework.stereotype.Component

@Component
class KommuneService(
        private val kommuneInfoClient: KommuneInfoClient
) {


    fun get(kommunenummer: String): KommuneInfo {
        return kommuneInfoClient.get(kommunenummer)
    }

    fun getBehandlingsanvarligKommune(kommunenummer: String): String? {
        val kommunenavn = kommuneInfoClient.get(kommunenummer).behandlingsansvarlig

        return if (kommunenavn != null) leggTilKommuneINavnet(kommunenavn) else null
    }

    private fun leggTilKommuneINavnet(kommunenavn: String): String {
       return if (kommunenavn.toLowerCase().endsWith(" kommune")) kommunenavn else "$kommunenavn kommune"
    }

    fun getAll(): List<KommuneInfo> {
        return kommuneInfoClient.getAll()
    }

    companion object {
        private val log by logger()
    }
}
