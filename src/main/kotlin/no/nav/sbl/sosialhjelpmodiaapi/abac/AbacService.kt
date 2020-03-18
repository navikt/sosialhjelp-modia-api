package no.nav.sbl.sosialhjelpmodiaapi.abac

import no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY
import no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_PEP_ID
import no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_DOMENE
import no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE
import no.nav.abac.xacml.StandardAttributter.ACTION_ID
import no.nav.sbl.sosialhjelpmodiaapi.common.TilgangskontrollException
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.BEARER
import no.nav.sbl.sosialhjelpmodiaapi.utils.SpringUtils
import org.springframework.stereotype.Component

@Component
class AbacService(private val abacClient: AbacClient,
                  private val springUtils: SpringUtils) {

    companion object {
        private val log by logger()
    }

    fun harTilgang(soker: String, token: String): Boolean {
        if (springUtils.isProfileMockOrLocal()) {
            return true
        }

        var tokenToUse = token
        if (token.startsWith(BEARER)){
            log.info("stripper bearer-prefiks fra token?")
            tokenToUse = token.substring(7)
        }
        val request = Request(
                environment = Attributes(mutableListOf(
                        Attribute(ENVIRONMENT_FELLES_PEP_ID, "srvsosialhjelp-mod"),
//                        Attribute("no.nav.abac.attributter.environment.felles.azure_jwt_token_body", token))), // azure token??
                        Attribute(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, tokenBody(tokenToUse)))),
                action = Attributes(mutableListOf()),
                resource = Attributes(mutableListOf(
                        Attribute(RESOURCE_FELLES_DOMENE, "sosialhjelp"),
                        Attribute(RESOURCE_FELLES_RESOURCE_TYPE, "no.nav.abac.attributter.resource.sosialhjelp"))),
                accessSubject = Attributes(mutableListOf())
        )
        val decision = abacClient.sjekkTilgang(request)
        return when (decision) {
            Decision.Permit -> true
            Decision.Deny -> false
            else -> throw TilgangskontrollException("Ukjent decision", null)
        }
    }

    fun ping(): Boolean {
        if (springUtils.isProfileMockOrLocal()) {
            return true
        }

        val request = Request(
                environment = Attributes(mutableListOf(
                        Attribute(ENVIRONMENT_FELLES_PEP_ID, "srvsosialhjelp-mod"))),
                action = Attributes(mutableListOf(
                        Attribute(ACTION_ID, "ping"))),
                resource = Attributes(mutableListOf(
                        Attribute(RESOURCE_FELLES_DOMENE, "sosialhjelp"))),
                accessSubject = null)

        val decision = abacClient.sjekkTilgang(request)
        return if (decision == Decision.Permit) true else throw RuntimeException("Abac - ping, decision != Permit")
    }

    private fun tokenBody(token: String): String {
        val parts = token.split('.')
        return if (parts.size == 1) parts[0] else parts[1]
    }
}