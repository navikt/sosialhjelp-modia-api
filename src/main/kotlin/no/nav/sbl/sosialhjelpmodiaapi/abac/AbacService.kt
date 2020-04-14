package no.nav.sbl.sosialhjelpmodiaapi.abac

import no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY
import no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_PEP_ID
import no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_DOMENE
import no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_PERSON_FNR
import no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE
import no.nav.abac.xacml.StandardAttributter.ACTION_ID
import no.nav.sbl.sosialhjelpmodiaapi.common.TilgangskontrollException
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.BEARER
import org.springframework.stereotype.Component

@Component
class AbacService(
        private val abacClient: AbacClient
) {

    companion object {
        private const val ENVIRONMENT_FELLES_AZURE_JWT_TOKEN_BODY = "no.nav.abac.attributter.environment.felles.azure_jwt_token_body"

        private val log by logger()
    }

    fun harTilgang(fnr: String, token: String) {
        val request = Request(
                environment = Attributes(mutableListOf(
                        Attribute(ENVIRONMENT_FELLES_PEP_ID, "srvsosialhjelp-mod"),
//                        Attribute(ENVIRONMENT_FELLES_AZURE_JWT_TOKEN_BODY, token))), // azure token eller oidc token?
                        Attribute(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, tokenBody(token)))),
                action = null,
                resource = Attributes(mutableListOf(
                        Attribute(RESOURCE_FELLES_DOMENE, "sosialhjelp"),
                        Attribute(RESOURCE_FELLES_RESOURCE_TYPE, "no.nav.abac.attributter.resource.sosialhjelp"),
                        Attribute(RESOURCE_FELLES_PERSON_FNR, fnr))),
                accessSubject = null)

        val abacResponse = abacClient.sjekkTilgang(request)
        if (abacResponse.decision != Decision.Permit) {
            log.warn("AbacResponse med decision=${abacResponse.decision}.")
            throw TilgangskontrollException("AbacResponse med decision=${abacResponse.decision}.")
        }
    }

    fun ping() {
        val request = Request(
                environment = Attributes(mutableListOf(
                        Attribute(ENVIRONMENT_FELLES_PEP_ID, "srvsosialhjelp-mod"))),
                action = Attributes(mutableListOf(
                        Attribute(ACTION_ID, "ping"))),
                resource = Attributes(mutableListOf(
                        Attribute(RESOURCE_FELLES_DOMENE, "sosialhjelp"))),
                accessSubject = null)

        val abacResponse = abacClient.sjekkTilgang(request)
        if (abacResponse.decision != Decision.Permit) {
            throw TilgangskontrollException("Abac - ping, decision er ikke Permit, men ${abacResponse.decision}.")
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

}