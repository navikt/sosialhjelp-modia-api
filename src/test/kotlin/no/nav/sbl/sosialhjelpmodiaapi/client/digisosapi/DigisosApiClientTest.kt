package no.nav.sbl.sosialhjelpmodiaapi.client.digisosapi

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.responses.ok_komplett_jsondigisossoker_response
import no.nav.sbl.sosialhjelpmodiaapi.service.idporten.IdPortenService
import no.nav.sbl.sosialhjelpmodiaapi.utils.DigisosApiWrapper
import no.nav.sbl.sosialhjelpmodiaapi.utils.SakWrapper
import no.nav.sosialhjelp.idporten.client.AccessToken
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

internal class DigisosApiClientTest {
    private val clientProperties: ClientProperties = mockk(relaxed = true)

    @Test
    fun `Post digisos sak til mock`() {
        val restTemplate: RestTemplate = mockk()
        val idPortenService: IdPortenService = mockk()

        val digisosApiClient = DigisosApiClientImpl(clientProperties, restTemplate, idPortenService)

        val mockResponse: ResponseEntity<String> = mockk()
        every { mockResponse.statusCode.is2xxSuccessful } returns true
        every { mockResponse.body } returns ok_komplett_jsondigisossoker_response
        every { idPortenService.getToken() } returns AccessToken("Token", 999)
        every {
            restTemplate.exchange(
                any<String>(),
                any(),
                any(),
                String::class.java
            )
        } returns mockResponse

        digisosApiClient.oppdaterDigisosSak("123123", DigisosApiWrapper(SakWrapper(JsonDigisosSoker()), ""))
    }
}
