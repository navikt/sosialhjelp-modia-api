package no.nav.sbl.sosialhjelpmodiaapi.service.digisosapi

import no.nav.sbl.sosialhjelpmodiaapi.client.digisosapi.DigisosApiClient
import no.nav.sbl.sosialhjelpmodiaapi.utils.DigisosApiWrapper
import org.springframework.stereotype.Component

@Component
class DigisosApiService(
        private val digisosApiClient: DigisosApiClient
) {

    fun oppdaterDigisosSak(fiksDigisosId: String?, digisosApiWrapper: DigisosApiWrapper): String? {
        return digisosApiClient.oppdaterDigisosSak(fiksDigisosId, digisosApiWrapper)
    }
}