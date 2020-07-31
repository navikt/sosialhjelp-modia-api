package no.nav.sbl.sosialhjelpmodiaapi.service.saksstatus

import no.nav.sbl.sosialhjelpmodiaapi.domain.Sak
import no.nav.sbl.sosialhjelpmodiaapi.domain.SaksStatus
import no.nav.sbl.sosialhjelpmodiaapi.domain.SaksStatusResponse
import no.nav.sbl.sosialhjelpmodiaapi.domain.VedtakResponse
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.springframework.stereotype.Component

const val DEFAULT_TITTEL: String = "Økonomisk sosialhjelp"

@Component
class SaksStatusService(
        private val fiksClient: FiksClient,
        private val eventService: EventService
) {

    fun hentSaksStatuser(fiksDigisosId: String): List<SaksStatusResponse> {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)
        val model = eventService.createModel(digisosSak)

        if (model.saker.isEmpty()) {
            log.info("Fant ingen saker for $fiksDigisosId")
            return emptyList()
        }

        val responseList = model.saker
                .filter { it.saksStatus != SaksStatus.FEILREGISTRERT }
                .map { sak ->
                    SaksStatusResponse(
                            tittel = sak.tittel ?: DEFAULT_TITTEL,
                            status = hentStatusNavn(sak),
                            vedtak = sak.vedtak.map {
                                VedtakResponse(
                                        vedtakDato = it.datoFattet,
                                        utfall = it.utfall
                                )
                            },
                            datoOpprettet = sak.datoOpprettet,
                            datoAvsluttet = sak.vedtak.maxBy { it.datoFattet }?.datoFattet
                    )
                }
        log.info("Hentet ${responseList.size} sak(er) for $fiksDigisosId")
        return responseList
    }

    private fun hentStatusNavn(sak: Sak): SaksStatus? {
        return when {
            sak.vedtak.isEmpty() -> sak.saksStatus ?: SaksStatus.UNDER_BEHANDLING
            else -> SaksStatus.FERDIGBEHANDLET
        }
    }

    companion object {
        private val log by logger()
    }
}