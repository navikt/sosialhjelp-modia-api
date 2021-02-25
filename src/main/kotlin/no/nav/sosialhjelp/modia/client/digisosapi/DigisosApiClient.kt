package no.nav.sosialhjelp.modia.client.digisosapi


import no.nav.sosialhjelp.modia.utils.DigisosApiWrapper
import org.springframework.stereotype.Component

@Component
interface DigisosApiClient {
    fun oppdaterDigisosSak(fiksDigisosId: String?, digisosApiWrapper: DigisosApiWrapper): String?
}
