package no.nav.sosialhjelp.modia.client.digisosapi

import io.mockk.every
import io.mockk.mockk
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.idporten.client.AccessToken
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.service.idporten.IdPortenService
import no.nav.sosialhjelp.modia.utils.DigisosApiWrapper
import no.nav.sosialhjelp.modia.utils.SakWrapper
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient

internal class DigisosApiClientTest {
    private val clientProperties: ClientProperties = mockk(relaxed = true)

    @Test
    fun `Post digisos sak til mock`() {
        val mockWebServer = MockWebServer()
        val fiksWebClient = WebClient.create(mockWebServer.url("/").toString())
        val idPortenService: IdPortenService = mockk()

        val digisosApiClient = DigisosApiClientImpl(clientProperties, fiksWebClient, idPortenService)

        every { idPortenService.getToken() } returns AccessToken("Token", 999)

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(202)
                .setBody("ok")
        )

        digisosApiClient.oppdaterDigisosSak("123123", DigisosApiWrapper(SakWrapper(JsonDigisosSoker()), ""))
    }
}
