package no.nav.sosialhjelp.modia.tilgang

import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.app.exceptions.ManglendeModiaSosialhjelpTilgangException
import no.nav.sosialhjelp.modia.app.exceptions.ManglendeTilgangException
import no.nav.sosialhjelp.modia.app.exceptions.PdlException
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.logging.Access
import no.nav.sosialhjelp.modia.logging.AuditService
import no.nav.sosialhjelp.modia.person.pdl.PdlClient
import no.nav.sosialhjelp.modia.person.pdl.PdlPerson
import no.nav.sosialhjelp.modia.person.pdl.isKode6Or7
import no.nav.sosialhjelp.modia.tilgang.azure.AzureGraphClient
import no.nav.sosialhjelp.modia.tilgang.skjermedepersoner.SkjermedePersonerClient
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Component
class TilgangskontrollService(
    private val pdlClient: PdlClient,
    private val skjermedePersonerClient: SkjermedePersonerClient,
    private val azureGraphClient: AzureGraphClient,
    private val auditService: AuditService,
    private val clientProperties: ClientProperties,
    private val env: Environment,
) {
    fun harTilgang(
        brukerIdent: String,
        token: String,
        url: String,
        method: HttpMethod,
    ) {
        val veilederToken = token.replace(BEARER, "")
        // TODO: Fjern når FSS er død
        if (!env.activeProfiles.contains("gcp") &&
            !azureGraphClient.hentInnloggetVeilederSineGrupper(veilederToken).value.any { it.id == clientProperties.veilederGruppeId }
        ) {
            throw ManglendeModiaSosialhjelpTilgangException("Veileder er ikke i riktig azure gruppe til å bruke modia sosialhjelp.")
                .also { auditService.reportToAuditlog(brukerIdent, url, method, Access.DENY) }
        }
        val pdlPerson =
            hentPersonFraPdl(brukerIdent, veilederToken)
                ?: throw ManglendeTilgangException("Person ikke funnet i PDL.")
                    .also { auditService.reportToAuditlog(brukerIdent, url, method, Access.DENY) }
        if (pdlPerson.isKode6Or7()) {
            throw ManglendeTilgangException("Person har addressebeskyttelse.")
                .also { auditService.reportToAuditlog(brukerIdent, url, method, Access.DENY) }
        }
        if (skjermedePersonerClient.erPersonSkjermet(brukerIdent, veilederToken)) {
            throw ManglendeTilgangException("Person er skjermet.")
                .also { auditService.reportToAuditlog(brukerIdent, url, method, Access.DENY) }
        }
    }

    fun harVeilederTilgangTilTjenesten(
        token: String,
        url: String,
        method: HttpMethod,
    ) {
        val veilederToken = token.replace(BEARER, "")
        if (!env.activeProfiles.contains("gcp") && !azureGraphClient.hentInnloggetVeilederSineGrupper(veilederToken).value.any { it.id == clientProperties.veilederGruppeId }) {
            throw ManglendeModiaSosialhjelpTilgangException("Veileder er ikke i riktig azure gruppe til å bruke modia sosialhjelp.")
                .also { auditService.reportToAuditlog("", url, method, Access.DENY) }
        }
    }

    private fun hentPersonFraPdl(
        brukerIdent: String,
        veilederToken: String,
    ): PdlPerson? =
        try {
            pdlClient.hentPerson(brukerIdent, veilederToken)?.hentPerson
        } catch (e: PdlException) {
            null
        }

    companion object {
        val log by logger()
    }
}
