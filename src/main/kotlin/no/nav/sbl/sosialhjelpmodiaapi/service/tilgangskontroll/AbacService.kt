package no.nav.sbl.sosialhjelpmodiaapi.service.tilgangskontroll

import no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY
import no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_PEP_ID
import no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_DOMENE
import no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_PERSON_FNR
import no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE
import no.nav.abac.xacml.StandardAttributter.ACTION_ID
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacClient
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacConstants.DENY_REASON
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacConstants.SOSIALHJELP_DOMENE
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacConstants.SOSIALHJELP_RESOURCE_TYPE
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.Advice
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.Attribute
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.Attributes
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.Decision
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.Request
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.manglerTilgangKode6Kode7EllerEgenAnsatt
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.manglerTilgangSosialhjelp
import no.nav.sbl.sosialhjelpmodiaapi.common.AbacException
import no.nav.sbl.sosialhjelpmodiaapi.common.ManglendeModiaSosialhjelpTilgangException
import no.nav.sbl.sosialhjelpmodiaapi.common.ManglendeTilgangException
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.BEARER
import org.springframework.stereotype.Component

@Component
class AbacService(
        private val abacClient: AbacClient
) {

    fun harTilgang(fnr: String, token: String) {
        val request = Request(
                environment = Attributes(mutableListOf(
                        Attribute(ENVIRONMENT_FELLES_PEP_ID, "srvsosialhjelp-mod"), // SUbjectHAndler.getconsumerId?
//                        Attribute(ENVIRONMENT_FELLES_AZURE_JWT_TOKEN_BODY, token))), // azure token eller oidc token?
                        Attribute(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, tokenBody(token)))),
                action = null,
                resource = Attributes(mutableListOf(
                        Attribute(RESOURCE_FELLES_DOMENE, SOSIALHJELP_DOMENE),
                        Attribute(RESOURCE_FELLES_RESOURCE_TYPE, SOSIALHJELP_RESOURCE_TYPE),
                        Attribute(RESOURCE_FELLES_PERSON_FNR, fnr))),
                accessSubject = null)

        val abacResponse = abacClient.sjekkTilgang(request)

        when (abacResponse.decision) {
            Decision.Permit -> {}
            Decision.Deny -> handleDenyAdvices(abacResponse.associatedAdvice)
            Decision.NotApplicable, Decision.Indeterminate -> throw AbacException("AbacResponse med decision=${abacResponse.decision}.")
        }
    }

    fun ping() {
        val request = Request(
                environment = Attributes(mutableListOf(
                        Attribute(ENVIRONMENT_FELLES_PEP_ID, "srvsosialhjelp-mod"))),
                action = Attributes(mutableListOf(
                        Attribute(ACTION_ID, "ping"))),
                resource = Attributes(mutableListOf(
                        Attribute(RESOURCE_FELLES_DOMENE, SOSIALHJELP_DOMENE))),
                accessSubject = null)

        val abacResponse = abacClient.sjekkTilgang(request)
        if (abacResponse.decision != Decision.Permit) {
            throw AbacException("Abac - ping, decision er ikke Permit, men ${abacResponse.decision}.")
        }
    }

    private fun tokenBody(token: String): String {
        val tokenToSplit = stripBearerPrefix(token)
        val parts = tokenToSplit.split('.')
        return if (parts.size == 1) parts[0] else parts[1]
    }

    private fun stripBearerPrefix(token: String): String {
        if (token.startsWith(BEARER)) {
            return token.substring(7)
        }
        return token
    }

    private fun handleDenyAdvices(advices: List<Advice>?) {
        val attributes: List<Attribute>? = advices
                ?.firstOrNull { it.id == DENY_REASON }?.attributeAssignment

        attributes?.let { handleDenyReasons(it) } ?: throw AbacException("Abac - fikk Deny, men ingen advices/attributes med forklaring.")
    }

    private fun handleDenyReasons(attributes: List<Attribute>) {
        attributes
                .find { it.manglerTilgangSosialhjelp() }
                ?.let { throw ManglendeModiaSosialhjelpTilgangException("Abac deny - veileder mangler tilgang til sosialhjelp (egen AD-rolle).")  }

        attributes
                .find { it.manglerTilgangKode6Kode7EllerEgenAnsatt() }
                ?.let { throw ManglendeTilgangException("Abac deny - veileder mangler tilgang til kode6/kode7/egenAnsatt")  }
    }

    companion object {
        private const val ENVIRONMENT_FELLES_AZURE_JWT_TOKEN_BODY = "no.nav.abac.attributter.environment.felles.azure_jwt_token_body"

        private val log by logger()
    }
}
