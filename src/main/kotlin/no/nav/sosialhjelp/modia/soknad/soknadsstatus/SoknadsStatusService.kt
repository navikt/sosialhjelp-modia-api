package no.nav.sosialhjelp.modia.soknad.soknadsstatus

import no.nav.sosialhjelp.modia.digisossak.event.EventService
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksClient
import no.nav.sosialhjelp.modia.logger
import org.springframework.stereotype.Component

@Component
class SoknadsStatusService(
    private val fiksClient: FiksClient,
    private val eventService: EventService
) {

    fun hentSoknadsStatus(fiksDigisosId: String): SoknadsStatusResponse {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)
        val model = eventService.createModel(digisosSak)
        log.info("Hentet nåværende søknadsstatus=${model.status.name} for $fiksDigisosId")
        return SoknadsStatusResponse(model.status)
    }

    companion object {
        private val log by logger()
    }
}
