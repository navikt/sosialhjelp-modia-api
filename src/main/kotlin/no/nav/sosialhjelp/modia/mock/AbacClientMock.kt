package no.nav.sosialhjelp.modia.mock

import no.nav.sosialhjelp.modia.client.abac.AbacClient
import no.nav.sosialhjelp.modia.client.abac.AbacResponse
import no.nav.sosialhjelp.modia.client.abac.Decision
import no.nav.sosialhjelp.modia.client.abac.Request
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("mock | local")
@Component
class AbacClientMock : AbacClient {

    override fun sjekkTilgang(request: Request): AbacResponse {
        return AbacResponse(Decision.Permit, emptyList())
    }
}
