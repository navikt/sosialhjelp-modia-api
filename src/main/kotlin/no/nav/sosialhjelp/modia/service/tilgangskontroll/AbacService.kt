package no.nav.sosialhjelp.modia.service.tilgangskontroll

import no.nav.sosialhjelp.modia.client.azure.AzureGraphClient
import no.nav.sosialhjelp.modia.client.pdl.PdlClient
import no.nav.sosialhjelp.modia.client.pdl.isKode6Or7
import no.nav.sosialhjelp.modia.client.skjermedePersoner.SkjermedePersonerClient
import no.nav.sosialhjelp.modia.common.ManglendeTilgangException
import no.nav.sosialhjelp.modia.common.PdlException
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.logging.AuditService
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Component
class AbacService(
    private val pdlClient: PdlClient,
    private val skjermedePersonerClient: SkjermedePersonerClient,
    private val azureGraphClient: AzureGraphClient,
    private val auditService: AuditService,
    private val clientProperties: ClientProperties
) {

    fun harTilgang(brukerIdent: String, token: String, url: String, method: HttpMethod) {
        val rawToken = token.replace(BEARER, "")
        if (!azureGraphClient.hentInnloggetVeilederSineGrupper(rawToken).value.any { it.id == clientProperties.veilederGruppeId })
            throw ManglendeTilgangException("Veileder er ikke i riktig azure gruppe til å bruke dialogløsningen.")
        // .also {}
        log.debug("Logget inn med gruppe ${clientProperties.veilederGruppeId}")
        val pdlPerson = pdlClient.hentPerson(brukerIdent)?.hentPerson
            ?: throw PdlException("Person ikke funnet i PDL.")
        // .also { auditService.reportToAuditlog(brukerIdent, url, method, Access.DENY) }
        if (pdlPerson.isKode6Or7()) throw ManglendeTilgangException("Person har addressebeskyttelse.")
        // .also { auditService.reportToAuditlog(brukerIdent, url, method, Access.DENY) }
        if (skjermedePersonerClient.erPersonSkjermet(brukerIdent)) throw ManglendeTilgangException("Person er skjermet.")
        // .also { auditService.reportToAuditlog(brukerIdent, url, method, Access.DENY) }
    }
    companion object {
        val log by logger()
    }
}
