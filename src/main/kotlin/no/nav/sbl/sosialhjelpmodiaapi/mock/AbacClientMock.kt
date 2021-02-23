package no.nav.sbl.sosialhjelpmodiaapi.mock

import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacClient
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacResponse
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.Decision
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.Request
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("mock | local")
@Component
class AbacClientMock : AbacClient {

    override fun sjekkTilgang(request: Request): AbacResponse {
        return AbacResponse(Decision.Permit, emptyList())
    }
}
