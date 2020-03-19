package no.nav.sbl.sosialhjelpmodiaapi.hendelse

import no.nav.sbl.sosialhjelpmodiaapi.domain.HendelseResponse
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.springframework.stereotype.Component


@Component
class HendelseService(private val fiksClient: FiksClient,
                      private val eventService: EventService) {

    fun hentHendelser(fiksDigisosId: String, sporingsId: String): List<HendelseResponse> {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId, sporingsId)
        val model = eventService.createModel(digisosSak, sporingsId)

        val responseList = model.historikk.map { HendelseResponse(it.tittel, it.tidspunkt.toString(), it.beskrivelse) }
        log.info("Hentet historikk for fiksDigisosId=$fiksDigisosId")
        return responseList
    }

    companion object {
        private val log by logger()
    }
}