package no.nav.sbl.sosialhjelpmodiaapi.abac

import no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY
import no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_PEP_ID
import no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_DOMENE
import no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE
import no.nav.abac.xacml.StandardAttributter.ACTION_ID
import no.nav.sbl.sosialhjelpmodiaapi.common.TilgangskontrollException
import org.springframework.stereotype.Component

@Component
class AbacService(private val abacClient: AbacClient) {

    fun harTilgang(soker: String, token: String): Boolean {
        val request = Request(
                environment = Attributes(mutableListOf(
                        Attribute(ENVIRONMENT_FELLES_PEP_ID, "srvsosialhjelp-mod"),
                        Attribute(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, token))),
                action = null,
                resource = Attributes(mutableListOf(
                        Attribute(RESOURCE_FELLES_DOMENE, "sosialhjelp"),
                        Attribute(RESOURCE_FELLES_RESOURCE_TYPE, "no.nav.abac.attributter.resource.sosialhjelp"))),
                accessSubject = null
        )
        val decision = abacClient.sjekkTilgang(request)
        return when (decision) {
            Decision.Permit -> true
            Decision.Deny -> false
            else -> throw TilgangskontrollException("Ukjent decision", null)
        }
    }

    fun ping(): Boolean {
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
}