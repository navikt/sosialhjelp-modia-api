package no.nav.sbl.sosialhjelpmodiaapi.client.fiks

import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksException
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.logging.AuditService
import no.nav.sbl.sosialhjelpmodiaapi.responses.ok_digisossak_response_string
import no.nav.sbl.sosialhjelpmodiaapi.responses.ok_minimal_jsondigisossoker_response_string
import no.nav.sbl.sosialhjelpmodiaapi.service.idporten.IdPortenService
import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate


internal class FiksClientTest {

    private val clientProperties: ClientProperties = mockk(relaxed = true)
    private val restTemplate: RestTemplate = mockk()
    private val idPortenService: IdPortenService = mockk()
    private val auditService: AuditService = mockk()

    private val fiksClient = FiksClientImpl(clientProperties, restTemplate, idPortenService, auditService)

    private val id = "123"

    @BeforeEach
    fun init() {
        clearAllMocks()

        every { idPortenService.getToken().token } returns "token"
        every { auditService.reportFiks(any(), any(), any(), any()) } just Runs
    }

    @Test
    fun `GET eksakt 1 DigisosSak`() {
        val mockResponse: ResponseEntity<DigisosSak> = mockk()
        val ok_digisossak_response = objectMapper.readValue(ok_digisossak_response_string, DigisosSak::class.java)
        every { mockResponse.body } returns ok_digisossak_response
        every {
            restTemplate.exchange(
                    any(),
                    any(),
                    any(),
                    DigisosSak::class.java,
                    any())
        } returns mockResponse

        val result = fiksClient.hentDigisosSak(id)

        assertNotNull(result)
    }

    @Test
    fun `GET DigisosSak feiler hvis Fiks gir 500`() {
        every {
            restTemplate.exchange(
                    any(),
                    any(),
                    any(),
                    String::class.java,
                    any())
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "some error")

        assertThatExceptionOfType(FiksException::class.java)
                .isThrownBy { fiksClient.hentDigisosSak(id) }
    }

    @Test
    fun `GET dokument`() {
        val mockResponse: ResponseEntity<JsonDigisosSoker> = mockk()
        val ok_minimal_jsondigisossoker_response = objectMapper.readValue(ok_minimal_jsondigisossoker_response_string, JsonDigisosSoker::class.java)
        every { mockResponse.body } returns ok_minimal_jsondigisossoker_response
        every {
            restTemplate.exchange(
                    any(),
                    any(),
                    any(),
                    JsonDigisosSoker::class.java,
                    any())
        } returns mockResponse

        val result = fiksClient.hentDokument("fnr", id, "dokumentlagerId", JsonDigisosSoker::class.java)

        assertNotNull(result)
    }
}
