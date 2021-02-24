package no.nav.sosialhjelp.modia.service.digisosapi

import no.nav.sosialhjelp.modia.client.digisosapi.DigisosApiClient
import no.nav.sosialhjelp.modia.utils.DigisosApiWrapper
import org.springframework.stereotype.Component

@Component
class DigisosApiService(
        private val digisosApiClient: DigisosApiClient
) {

    fun oppdaterDigisosSak(fiksDigisosId: String?, digisosApiWrapper: DigisosApiWrapper): String? {
        return digisosApiClient.oppdaterDigisosSak(fiksDigisosId, digisosApiWrapper)
    }
}