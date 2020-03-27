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
import no.nav.sbl.sosialhjelpmodiaapi.utils.MiljoUtils
import org.springframework.stereotype.Component

@Component
class AbacService(
        private val abacClient: AbacClient,
        private val miljoUtils: MiljoUtils
) {

    companion object {
        private val log by logger()
    }

    fun harTilgang(fnr: String, token: String): Boolean {
        if (miljoUtils.isProfileMockOrLocal()) {
            return true
        }

        val request = Request(
                environment = Attributes(mutableListOf(
                        Attribute(ENVIRONMENT_FELLES_PEP_ID, "srvsosialhjelp-mod"),
//                        Attribute("no.nav.abac.attributter.environment.felles.azure_jwt_token_body", token))), // azure token??
                        Attribute(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, tokenBody(stripBearerPrefix(token))))),
                action = Attributes(mutableListOf()),
                resource = Attributes(mutableListOf(
                        Attribute(RESOURCE_FELLES_DOMENE, "sosialhjelp"),
                        Attribute(RESOURCE_FELLES_RESOURCE_TYPE, "no.nav.abac.attributter.resource.sosialhjelp"),
                        Attribute(RESOURCE_FELLES_PERSON_FNR, fnr))),
                accessSubject = Attributes(mutableListOf())
        )
        val abacResponse = abacClient.sjekkTilgang(request)
        return when (abacResponse.decision) {
            Decision.Permit -> true
            else -> {
                log.warn("AbacResponse er ${abacResponse.decision}.")
                false
            }
        }
    }

    fun ping(): Boolean {
        if (miljoUtils.isProfileMockOrLocal()) {
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

        val abacResponse = abacClient.sjekkTilgang(request)
        return if (abacResponse.decision == Decision.Permit){
           true
        } else {
            throw TilgangskontrollException("Abac - ping, decision er ikke Permit, men ${abacResponse.decision}.")
        }
    }

    private fun stripBearerPrefix(token: String): String {
        if (token.startsWith(BEARER)){
            log.info("stripper bearer-prefiks fra token(!)")
            return token.substring(7)
        }
        return token
    }

    private fun tokenBody(token: String): String {
        val parts = token.split('.')
        return if (parts.size == 1) parts[0] else parts[1]
    }
}