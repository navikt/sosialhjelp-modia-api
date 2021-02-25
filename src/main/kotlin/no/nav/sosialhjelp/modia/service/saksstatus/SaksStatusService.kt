package no.nav.sosialhjelp.modia.service.saksstatus

import no.nav.sosialhjelp.modia.domain.Sak
import no.nav.sosialhjelp.modia.domain.SaksStatus
import no.nav.sosialhjelp.modia.domain.SaksStatusResponse
import no.nav.sosialhjelp.modia.domain.VedtakResponse
import no.nav.sosialhjelp.modia.event.EventService
import no.nav.sosialhjelp.modia.client.fiks.FiksClient
import no.nav.sosialhjelp.modia.logger
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
                            datoAvsluttet = sak.vedtak.maxByOrNull { it.datoFattet }?.datoFattet
                    )
                }
        log.info("Hentet ${responseList.size} sak(er) for $fiksDigisosId")
        return responseList
    }

    private fun hentStatusNavn(sak: Sak): SaksStatus {
        return when {
            sak.vedtak.isEmpty() -> sak.saksStatus ?: SaksStatus.UNDER_BEHANDLING
            else -> SaksStatus.FERDIGBEHANDLET
        }
    }

    companion object {
        private val log by logger()
    }
}