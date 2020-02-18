package no.nav.sbl.sosialhjelpmodiaapi.soknadsoversikt

import no.nav.sbl.sosialhjelpmodiaapi.common.FiksException
import no.nav.sbl.sosialhjelpmodiaapi.domain.*
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.oppgave.OppgaveService
import no.nav.sbl.sosialhjelpmodiaapi.saksstatus.DEFAULT_TITTEL
import no.nav.sbl.sosialhjelpmodiaapi.unixTimestampToDate
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@ProtectedWithClaims(issuer = "veileder")
@RestController
@RequestMapping("/api/v1/innsyn")
class SoknadsoversiktController(private val fiksClient: FiksClient,
                                private val eventService: EventService,
                                private val oppgaveService: OppgaveService) {

    @GetMapping("/saker", produces = ["application/json;charset=UTF-8"])
    fun hentAlleSaker(@RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String): ResponseEntity<List<SaksListeResponse>> {
        val saker = try {
            fiksClient.hentAlleDigisosSaker(token)
        } catch (e: FiksException) {
            return ResponseEntity.status(503).build()
        }

        val responselist = saker
                .map { sak ->
                    SaksListeResponse(
                            fiksDigisosId = sak.fiksDigisosId,
                            soknadTittel = "Søknad om økonomisk sosialhjelp",
                            sistOppdatert = unixTimestampToDate(sak.sistEndret),
                            sendt = sak.originalSoknadNAV?.timestampSendt?.let { unixTimestampToDate(it) },
                            kilde = IntegrationUtils.KILDE_INNSYN_API
                    )
                }
        log.info("Hentet alle (${responselist.size}) DigisosSaker for bruker.")

        return ResponseEntity.ok().body(responselist.sortedByDescending { it.sistOppdatert })
    }

    @GetMapping("{fiksDigisosId}/saksDetaljer", produces = ["application/json;charset=UTF-8"])
    fun hentSaksDetaljer(@PathVariable fiksDigisosId: String, @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String): ResponseEntity<SaksDetaljerResponse> {
        val sak = fiksClient.hentDigisosSak(fiksDigisosId, token)
        val model = eventService.createSoknadsoversiktModel(token, sak)
        val saksDetaljerResponse = SaksDetaljerResponse(
                fiksDigisosId = sak.fiksDigisosId,
                soknadTittel = hentNavn(model),
                status = model.status?.let { mapStatus(it) } ?: "",
                harNyeOppgaver = harNyeOppgaver(model, sak.fiksDigisosId, token),
                harVilkar = harVilkar(model)
        )
        return ResponseEntity.ok().body(saksDetaljerResponse)
    }

    private fun mapStatus(status: SoknadsStatus): String {
        return if (status == SoknadsStatus.BEHANDLES_IKKE) {
            SoknadsStatus.FERDIGBEHANDLET.name
        } else {
            status.name.replace('_', ' ')
        }
    }

    private fun hentNavn(model: InternalDigisosSoker): String {
        return model.saker.filter { SaksStatus.FEILREGISTRERT != it.saksStatus }.joinToString { it.tittel ?: DEFAULT_TITTEL }
    }

    private fun harNyeOppgaver(model: InternalDigisosSoker, fiksDigisosId: String, token: String): Boolean {
        return when {
            model.oppgaver.isEmpty() -> false
            else -> oppgaveService.hentOppgaver(fiksDigisosId, token).isNotEmpty()
        }
    }

    private fun harVilkar(model: InternalDigisosSoker): Boolean {
        // forenkle?
        return model.saker
                .any { sak -> sak.vilkar
                        .any { vilkar -> !vilkar.oppfyllt } }
    }

    companion object {
        private val log by logger()
    }
}