package no.nav.sbl.sosialhjelpmodiaapi.abac

import no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY
import no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_PEP_ID
import no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_DOMENE
import no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_PERSON_TILKNYTTET_FNR
import no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE
import no.nav.sbl.sosialhjelpmodiaapi.common.TilgangskontrollException
import org.springframework.stereotype.Component

@Component
class AbacService (private val client: AbacClient) {

    fun harTilgang(soker: String, token: String): Boolean {

        val request = Request(
                environment = Attributes(mutableListOf(
                        Attribute(ENVIRONMENT_FELLES_PEP_ID, "srvsosialhjelp-mod"),
                        Attribute(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, token))),
                action = null,
                resource = Attributes(mutableListOf(
                        Attribute(RESOURCE_FELLES_DOMENE, "domene for digisos"), //TODO?
                        Attribute(RESOURCE_FELLES_RESOURCE_TYPE, "no.nav.abac.attributter.resource.sosialhjelp"),
                        Attribute(RESOURCE_FELLES_PERSON_TILKNYTTET_FNR, soker))),
                accessSubject = null
        )
        val decision = client.sjekkTilgang(request)
        return when (decision) {
            Decision.Permit -> true
            Decision.Deny -> false
            else -> throw TilgangskontrollException("Ukjent decision", null)
        }
    }

    fun ping(): Boolean {
        val decision = client.ping()
        return if (decision == Decision.Permit) true else throw RuntimeException("Abac - ping, decision != Permit")
    }
}