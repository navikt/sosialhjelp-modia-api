package no.nav.sosialhjelp.modia.tilgang

import no.nav.sosialhjelp.modia.app.exceptions.ManglendeTilgangException
import no.nav.sosialhjelp.modia.app.exceptions.PdlException
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.logging.Access
import no.nav.sosialhjelp.modia.logging.AuditService
import no.nav.sosialhjelp.modia.person.pdl.PdlClient
import no.nav.sosialhjelp.modia.person.pdl.PdlPerson
import no.nav.sosialhjelp.modia.person.pdl.isKode6Or7
import no.nav.sosialhjelp.modia.tilgang.skjermedepersoner.SkjermedePersonerClient
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Component
class TilgangskontrollService(
    private val pdlClient: PdlClient,
    private val skjermedePersonerClient: SkjermedePersonerClient,
    private val auditService: AuditService,
) {
    suspend fun harTilgang(
        brukerIdent: String,
        token: String,
        url: String,
        method: HttpMethod,
    ) {
        val veilederToken = token.replace(BEARER, "")
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

    private suspend fun hentPersonFraPdl(
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
