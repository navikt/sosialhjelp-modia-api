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

    fun getAll(): List<KommuneInfo> {
        return kommuneInfoClient.getAll()
    }

    companion object {
        private val log by logger()
    }
}
