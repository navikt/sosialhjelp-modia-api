package no.nav.sosialhjelp.modia.app.maskinporten

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.modia.auth.texas.TexasClient

// TODO: Fjern denne når fss er borte og bruk texasClient direkte.
class MaskinportenClientGcp(
    private val texasClient: TexasClient,
) : MaskinportenClient {
    override fun getToken(): String =
        runBlocking(Dispatchers.IO) {
            texasClient.getMaskinportenToken()
        }
}
