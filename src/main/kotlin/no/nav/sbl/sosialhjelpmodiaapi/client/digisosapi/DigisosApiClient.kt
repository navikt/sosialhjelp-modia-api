package no.nav.sbl.sosialhjelpmodiaapi.client.digisosapi

import no.nav.sbl.sosialhjelpmodiaapi.utils.DigisosApiWrapper
import org.springframework.stereotype.Component

@Component
interface DigisosApiClient {
    fun oppdaterDigisosSak(fiksDigisosId: String?, digisosApiWrapper: DigisosApiWrapper): String?
}
