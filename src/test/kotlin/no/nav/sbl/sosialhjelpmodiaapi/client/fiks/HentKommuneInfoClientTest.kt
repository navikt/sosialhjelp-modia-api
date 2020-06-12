package no.nav.sbl.sosialhjelpmodiaapi.client.fiks

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.client.idporten.IdPortenService
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksException
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import kotlin.test.assertNotNull

internal class HentKommuneInfoClientTest {

    private val clientProperties: ClientProperties = mockk(relaxed = true)
    private val restTemplate: RestTemplate = mockk()
    private val idPortenService: IdPortenService = mockk()

    private val hentKommuneInfoClient = HentKommuneInfoClient(restTemplate, clientProperties, idPortenService)

    @BeforeEach
    fun init() {
        clearMocks(restTemplate)
    }

    @Test
    fun `GET KommuneInfo for kommunenummer`() {
        val mockKommuneResponse: ResponseEntity<KommuneInfo> = mockk()
        val mockKommuneInfo: KommuneInfo = mockk()

        every { mockKommuneResponse.statusCode.is2xxSuccessful } returns true
        every { mockKommuneResponse.body } returns mockKommuneInfo

        coEvery { idPortenService.requestToken().token } returns "token"

        every {
            restTemplate.exchange(
                    any(),
                    HttpMethod.GET,
                    any(),
                    KommuneInfo::class.java,
                    any())
        } returns mockKommuneResponse

        val result = hentKommuneInfoClient.hentKommuneInfo("1234")

        assertNotNull(result)
    }

    @Test
    fun `GET KommuneInfo feiler hvis kommuneInfo gir 404`() {
        val mockKommuneResponse: ResponseEntity<KommuneInfo> = mockk()
        val mockKommuneInfo: KommuneInfo = mockk()

        every { mockKommuneResponse.statusCode.is2xxSuccessful } returns true
        every { mockKommuneResponse.body } returns mockKommuneInfo

        coEvery { idPortenService.requestToken().token } returns "token"

        every {
            restTemplate.exchange(
                    any(),
                    HttpMethod.GET,
                    any(),
                    KommuneInfo::class.java,
                    any())
        } throws HttpClientErrorException(HttpStatus.NOT_FOUND, "not found")

        Assertions.assertThatExceptionOfType(FiksException::class.java).isThrownBy { hentKommuneInfoClient.hentKommuneInfo("1234") }
    }
}