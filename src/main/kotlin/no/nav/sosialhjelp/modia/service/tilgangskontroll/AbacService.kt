package no.nav.sosialhjelp.modia.service.tilgangskontroll

import no.nav.sosialhjelp.modia.client.abac.AbacClient
import no.nav.sosialhjelp.modia.client.pdl.PdlClient
import no.nav.sosialhjelp.modia.client.pdl.isKode6Or7
import no.nav.sosialhjelp.modia.client.skjermedePersoner.SkjermedePersonerClient
import no.nav.sosialhjelp.modia.common.ManglendeTilgangException
import no.nav.sosialhjelp.modia.common.PdlException
import no.nav.sosialhjelp.modia.logging.AuditService
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Component
class AbacService(
    private val abacClient: AbacClient,
    private val pdlClient: PdlClient,
    private val skjermedePersonerClient: SkjermedePersonerClient,
//    private val azureGraphClient: AzureGraphClient,
    private val auditService: AuditService
) {

    fun harTilgang(brukerIdent: String, token: String, url: String, method: HttpMethod) {
        val pdlPerson = pdlClient.hentPerson(brukerIdent)?.hentPerson
            ?: throw PdlException("Person ikke funnet i PDL.")
        // .also { auditService.reportToAuditlog(brukerIdent, url, method, Access.DENY) }
        if (pdlPerson.isKode6Or7()) throw ManglendeTilgangException("Person har addressebeskyttelse.")
        // .also { auditService.reportToAuditlog(brukerIdent, url, method, Access.DENY) }
        if (skjermedePersonerClient.erPersonSkjermet(brukerIdent)) throw ManglendeTilgangException("Person er skjermet.")
        // .also { auditService.reportToAuditlog(brukerIdent, url, method, Access.DENY) }

//        val request = Request(
//            environment = Attributes(
//                mutableListOf(
//                    Attribute(ENVIRONMENT_FELLES_PEP_ID, SRVSOSIALHJELP_MOD),
//                    Attribute(ENVIRONMENT_FELLES_AZURE_JWT_TOKEN_BODY, tokenBody(token))
//                )
//            ),
//            action = null,
//            resource = Attributes(
//                mutableListOf(
//                    Attribute(RESOURCE_FELLES_DOMENE, SOSIALHJELP_DOMENE),
//                    Attribute(RESOURCE_FELLES_RESOURCE_TYPE, SOSIALHJELP_RESOURCE_TYPE),
//                    Attribute(RESOURCE_FELLES_PERSON_FNR, brukerIdent)
//                )
//            ),
//            accessSubject = null
//        )
//
//        val abacResponse = abacClient.sjekkTilgang(request)
//
//        when (abacResponse.decision) {
//            Decision.Permit -> {}
//            Decision.Deny -> handleDenyAdvices(abacResponse.associatedAdvice)
//            Decision.NotApplicable, Decision.Indeterminate -> throw AbacException("AbacResponse med decision=${abacResponse.decision}.")
//        }
    }
}
