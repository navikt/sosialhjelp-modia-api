package no.nav.sosialhjelp.modia.service.soknadsstatus

import no.nav.sosialhjelp.modia.domain.SoknadsStatusResponse
import no.nav.sosialhjelp.modia.event.EventService
import no.nav.sosialhjelp.modia.client.fiks.FiksClient
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
        val status = model.status
        if (status == null) {
            log.warn("SoknadsStatus kan ikke være null")
            throw RuntimeException("SoknadsStatus kan ikke være null")
        }
        log.info("Hentet nåværende søknadsstatus=${status.name} for $fiksDigisosId")
        return SoknadsStatusResponse(status)
    }

    companion object {
        private val log by logger()
    }
}