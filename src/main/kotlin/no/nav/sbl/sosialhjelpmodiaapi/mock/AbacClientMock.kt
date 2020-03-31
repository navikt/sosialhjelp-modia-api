package no.nav.sbl.sosialhjelpmodiaapi.mock

import no.nav.sbl.sosialhjelpmodiaapi.abac.AbacClient
import no.nav.sbl.sosialhjelpmodiaapi.abac.AbacResponse
import no.nav.sbl.sosialhjelpmodiaapi.abac.Decision
import no.nav.sbl.sosialhjelpmodiaapi.abac.Request
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("mock | local")
@Component
class AbacClientMock : AbacClient {

    override fun sjekkTilgang(request: Request): AbacResponse {
        return AbacResponse(Decision.Permit, emptyList())
    }

}