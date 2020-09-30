package no.nav.sbl.sosialhjelpmodiaapi.client.fiks

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksException
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.logging.AuditService
import no.nav.sbl.sosialhjelpmodiaapi.redis.RedisService
import no.nav.sbl.sosialhjelpmodiaapi.responses.ok_digisossak_response_string
import no.nav.sbl.sosialhjelpmodiaapi.responses.ok_minimal_jsondigisossoker_response_string
import no.nav.sbl.sosialhjelpmodiaapi.service.idporten.IdPortenService
import no.nav.sbl.sosialhjelpmodiaapi.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sbl.sosialhjelpmodiaapi.subjecthandler.SubjectHandlerUtils
import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
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
    private val redisService: RedisService = mockk()

    private val fiksClient = FiksClientImpl(clientProperties, restTemplate, idPortenService, auditService, redisService)

    private val id = "123"

    @BeforeEach
    fun init() {
        clearAllMocks()

        every { idPortenService.getToken().token } returns "token"
        every { auditService.reportFiks(any(), any(), any(), any()) } just Runs

        every { redisService.get(any(), any()) } returns null
        every { redisService.put(any(), any()) } just Runs

        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    internal fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
    }

    @Test
    fun `GET eksakt 1 DigisosSak`() {
        val mockResponse: ResponseEntity<DigisosSak> = mockk()
        val digisosSak = objectMapper.readValue(ok_digisossak_response_string, DigisosSak::class.java)
        every { mockResponse.body } returns digisosSak
        every {
            restTemplate.exchange(
                    any(),
                    any(),
                    any(),
                    DigisosSak::class.java,
                    any())
        } returns mockResponse

        val result = fiksClient.hentDigisosSak(id)

        assertThat(result).isNotNull
    }

    @Test
    fun `GET DigisosSak feiler hvis Fiks gir 500`() {
        every {
            restTemplate.exchange(
                    any(),
                    any(),
                    any(),
                    DigisosSak::class.java,
                    any())
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "some error")

        assertThatExceptionOfType(FiksException::class.java)
                .isThrownBy { fiksClient.hentDigisosSak(id) }
    }

    @Test
    fun `GET digisosSak fra cache`() {
        val digisosSak: DigisosSak = objectMapper.readValue(ok_digisossak_response_string)
        every { redisService.get(any(), DigisosSak::class.java) } returns digisosSak

        val result2 = fiksClient.hentDigisosSak(id)

        assertThat(result2).isNotNull

        verify(exactly = 0) { restTemplate.exchange(any(), any(), any(), DigisosSak::class.java, any()) }
        verify(exactly = 0) { redisService.put(any(), any()) }
    }

    @Test
    fun `GET digisosSak fra cache etter put`() {
        val mockResponse: ResponseEntity<DigisosSak> = mockk()
        val digisosSak = objectMapper.readValue(ok_digisossak_response_string, DigisosSak::class.java)
        every { mockResponse.body } returns digisosSak
        every {
            restTemplate.exchange(
                    any(),
                    any(),
                    any(),
                    DigisosSak::class.java,
                    any())
        } returns mockResponse

        val result1 = fiksClient.hentDigisosSak(id)

        assertThat(result1).isNotNull
        verify(exactly = 1) { redisService.get(any(), DigisosSak::class.java) }
        verify(exactly = 1) { restTemplate.exchange(any(), any(), any(), DigisosSak::class.java, any()) }
        verify(exactly = 1) { redisService.put(any(), any()) }

        every { redisService.get(any(), DigisosSak::class.java) } returns digisosSak

        val result = fiksClient.hentDigisosSak(id)

        assertThat(result).isNotNull
        verify(exactly = 2) { redisService.get(any(), any()) }
        verify(exactly = 1) { restTemplate.exchange(any(), any(), any(), DigisosSak::class.java, any()) }
        verify(exactly = 1) { redisService.put(any(), any()) }
    }

    @Test
    fun `GET digisosSak fra annen saksbehandler`() {
        val mockResponse: ResponseEntity<DigisosSak> = mockk()
        val digisosSak = objectMapper.readValue(ok_digisossak_response_string, DigisosSak::class.java)
        every { mockResponse.body } returns digisosSak
        every {
            restTemplate.exchange(
                    any(),
                    any(),
                    any(),
                    DigisosSak::class.java,
                    any())
        } returns mockResponse

//        annen veileder har hentet digisosSak tidligere (og finnes i cache)
        val annenSaksbehandler = "other"
        every { redisService.get("${annenSaksbehandler}_$id", DigisosSak::class.java) } returns digisosSak

        val result = fiksClient.hentDigisosSak(id)

        assertThat(result).isNotNull
        verify(exactly = 1) { redisService.get(any(), any()) }
        verify(exactly = 1) { restTemplate.exchange(any(), any(), any(), DigisosSak::class.java, any()) }
        verify(exactly = 1) { redisService.put(any(), any()) }
    }

    @Test
    fun `GET dokument`() {
        val mockResponse: ResponseEntity<JsonDigisosSoker> = mockk()
        val jsonDigisosSoker = objectMapper.readValue(ok_minimal_jsondigisossoker_response_string, JsonDigisosSoker::class.java)
        every { mockResponse.body } returns jsonDigisosSoker
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

    @Test
    fun `GET dokument fra cache`() {
        val jsonDigisosSoker: JsonDigisosSoker = objectMapper.readValue(ok_minimal_jsondigisossoker_response_string)
        every { redisService.get(any(), JsonDigisosSoker::class.java) } returns jsonDigisosSoker

        val result = fiksClient.hentDokument("fnr", id, "dokumentlagerId", JsonDigisosSoker::class.java)

        assertNotNull(result)
        verify(exactly = 0) { restTemplate.exchange(any(), any(), any(), JsonDigisosSoker::class.java, any()) }
        verify(exactly = 0) { redisService.put(any(), any()) }
    }
}