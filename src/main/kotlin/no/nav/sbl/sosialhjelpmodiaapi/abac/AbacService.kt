package no.nav.sbl.sosialhjelpmodiaapi.abac

import no.nav.sbl.sosialhjelpmodiaapi.common.TilgangskontrollException
import org.springframework.stereotype.Component

@Component
class AbacService (private val client: AbacClient) {

    fun harTilgang(saksbehandler: String, soker: String): Boolean {

//        val request = Request()
//        val decision = client.sjekkTilgang(request)
//        if (decision == Decision.Permit) {
//            return true
//        } else if (decision == Decision.Deny) {
//            return false
//        } else {
//            throw TilgangskontrollException("Ukjent decision", null)
//        }

        return true
    }
}