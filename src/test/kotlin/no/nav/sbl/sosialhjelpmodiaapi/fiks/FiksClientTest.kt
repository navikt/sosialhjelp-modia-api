package no.nav.sbl.sosialhjelpmodiaapi.fiks

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksException
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.domain.KommuneInfo
import no.nav.sbl.sosialhjelpmodiaapi.idporten.IdPortenService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate


internal class FiksClientTest {

    private val clientProperties: ClientProperties = mockk(relaxed = true)
    private val restTemplate: RestTemplate = mockk()
    private val idPortenService: IdPortenService = mockk()

    private val fiksClient = FiksClientImpl(clientProperties, restTemplate, idPortenService)

    private val id = "123"

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
                    any<String>(),
                    HttpMethod.GET,
                    any(),
                    KommuneInfo::class.java)
        } returns mockKommuneResponse

        val result = fiksClient.hentKommuneInfo("1234")

        assertThat(result).isNotNull
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
                    any<String>(),
                    HttpMethod.GET,
                    any(),
                    KommuneInfo::class.java)
        } throws HttpClientErrorException(HttpStatus.NOT_FOUND, "not found")

        assertThatExceptionOfType(FiksException::class.java).isThrownBy { fiksClient.hentKommuneInfo("1234") }
    }
}