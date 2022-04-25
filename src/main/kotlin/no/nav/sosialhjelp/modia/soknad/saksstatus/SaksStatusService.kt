package no.nav.sosialhjelp.modia.soknad.saksstatus

import no.nav.sosialhjelp.modia.digisossak.domain.Sak
import no.nav.sosialhjelp.modia.digisossak.domain.SaksStatus
import no.nav.sosialhjelp.modia.digisossak.event.EventService
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksClient
import no.nav.sosialhjelp.modia.digisossak.event.SAK_DEFAULT_TITTEL
import no.nav.sosialhjelp.modia.logger
import org.springframework.stereotype.Component

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
                    tittel = sak.tittel ?: SAK_DEFAULT_TITTEL,
                    status = hentStatusNavn(sak),
                    vedtak = sak.vedtak.map {
                        SaksStatusResponse.Vedtak(
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
