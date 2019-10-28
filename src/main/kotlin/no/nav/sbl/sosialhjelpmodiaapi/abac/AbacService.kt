package no.nav.sbl.sosialhjelpmodiaapi.abac

import org.springframework.stereotype.Component

@Component
class AbacService (private val client: AbacClient) {

    fun sjekkTilgang(saksbehandler: String, soker: String) {

//        val request = Request()
//        client.harTilgang(request)

    }
}