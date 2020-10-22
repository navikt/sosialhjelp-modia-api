package no.nav.sbl.sosialhjelpmodiaapi.mock

import no.nav.sbl.sosialhjelpmodiaapi.client.msgraph.MsGraphClient
import no.nav.sbl.sosialhjelpmodiaapi.client.msgraph.OnPremisesSamAccountName
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("mock")
@Component
class MsGraphClientMock : MsGraphClient {

    val defaultResponse = OnPremisesSamAccountName("Z123456")

    override fun hentOnPremisesSamAccountName(): OnPremisesSamAccountName {
        return defaultResponse
    }
}