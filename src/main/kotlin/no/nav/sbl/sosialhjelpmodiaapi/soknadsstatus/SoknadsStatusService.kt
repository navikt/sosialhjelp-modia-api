package no.nav.sbl.sosialhjelpmodiaapi.soknadsstatus

import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsStatusResponse
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.springframework.stereotype.Component


@Component
class SoknadsStatusService(private val eventService: EventService) {

    companion object {
        val log by logger()
    }

    fun hentSoknadsStatus(fiksDigisosId: String, token: String): SoknadsStatusResponse {
        val model = eventService.createModel(fiksDigisosId, token)
        val status = model.status
        if (status == null) {
            log.warn("SoknadsStatus kan ikke være null")
            throw RuntimeException("SoknadsStatus kan ikke være null")
        }
        log.info("Hentet nåværende søknadsstatus=${status.name} for $fiksDigisosId")
        return SoknadsStatusResponse(status)
    }
}