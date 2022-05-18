package no.nav.sosialhjelp.modia.soknadoversikt

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.OppgaveStatus
import no.nav.sosialhjelp.modia.digisossak.event.EventService
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksClient
import no.nav.sosialhjelp.modia.hentSoknadTittel
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.soknad.dokumentasjonkrav.DokumentasjonkravService
import no.nav.sosialhjelp.modia.soknad.oppgave.OppgaveService
import no.nav.sosialhjelp.modia.tilgang.TilgangskontrollService
import no.nav.sosialhjelp.modia.unixTimestampToDate
import no.nav.sosialhjelp.modia.utils.Ident
import no.nav.sosialhjelp.modia.utils.IntegrationUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "azuread")
@RestController
@RequestMapping("/api", produces = ["application/json;charset=UTF-8"], consumes = ["application/json;charset=UTF-8"])
class SoknadsoversiktController(
    private val fiksClient: FiksClient,
    private val eventService: EventService,
    private val oppgaveService: OppgaveService,
    private val dokumentasjonkravService: DokumentasjonkravService,
    private val tilgangskontrollService: TilgangskontrollService
) {
    @PostMapping("/soknader")
    fun getSoknader(@RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<List<SoknadResponse>> {
        tilgangskontrollService.harTilgang(ident.fnr, token, "/soknader", HttpMethod.POST)

        val saker = try {
            fiksClient.hentAlleDigisosSaker(ident.fnr)
        } catch (e: FiksException) {
            return ResponseEntity.status(503).build()
        }

        val responselist = saker
            .map { sak ->
                SoknadResponse(
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

    @PostMapping("/{fiksDigisosId}/soknadDetaljer")
    fun getSoknadDetaljer(@PathVariable fiksDigisosId: String, @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String, @RequestBody ident: Ident): ResponseEntity<SoknadDetaljerResponse> {
        tilgangskontrollService.harTilgang(ident.fnr, token, "/$fiksDigisosId/soknadDetaljer", HttpMethod.POST)

        val sak = fiksClient.hentDigisosSak(fiksDigisosId)
        val model = eventService.createSoknadsoversiktModel(sak)
        val saksDetaljerResponse = SoknadDetaljerResponse(
            fiksDigisosId = sak.fiksDigisosId,
            soknadTittel = hentSoknadTittel(sak, model),
            status = model.status,
            manglerOpplysninger = manglerOpplysninger(model, sak.fiksDigisosId),
            harNyeOppgaver = harNyeOppgaver(model, sak.fiksDigisosId),
            harDokumentasjonkrav = harDokumentasjonkrav(model, sak.fiksDigisosId),
            harVilkar = harVilkar(model)
        )
        return ResponseEntity.ok().body(saksDetaljerResponse)
    }

    private fun manglerOpplysninger(model: InternalDigisosSoker, fiksDigisosId: String): Boolean {
        return when {
            model.oppgaver.isEmpty() && model.dokumentasjonkrav.isEmpty() -> false
            else -> oppgaveService.hentOppgaver(fiksDigisosId).isNotEmpty() || dokumentasjonkravService.hentDokumentasjonkrav(fiksDigisosId).isNotEmpty()
        }
    }

    private fun harDokumentasjonkrav(model: InternalDigisosSoker, fiksDigisosId: String): Boolean{
        return when { model.dokumentasjonkrav.isEmpty() -> false
            else -> dokumentasjonkravService.hentDokumentasjonkrav(fiksDigisosId).isNotEmpty()
        }
    }
    
    private fun harNyeOppgaver(model: InternalDigisosSoker, fiksDigisosId: String): Boolean {
        return when {
            model.oppgaver.isEmpty() -> false
            else -> oppgaveService.hentOppgaver(fiksDigisosId).isNotEmpty()
        }
    }

    private fun harVilkar(model: InternalDigisosSoker): Boolean {
        return model.vilkar
            .any { vilkar -> vilkar.status == OppgaveStatus.RELEVANT }
    }

    companion object {
        private val log by logger()
    }
}
