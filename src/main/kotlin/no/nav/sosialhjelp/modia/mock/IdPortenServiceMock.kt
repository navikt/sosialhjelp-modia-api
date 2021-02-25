package no.nav.sosialhjelp.modia.mock

import no.nav.sosialhjelp.modia.service.idporten.IdPortenService
import no.nav.sosialhjelp.idporten.client.AccessToken
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("mock")
class IdPortenServiceMock : IdPortenService {

    override fun getToken(): AccessToken {
        return AccessToken("something something token here", 1234)
    }

}