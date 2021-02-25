package no.nav.sosialhjelp.modia.client.fiks

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.finn.unleash.Unleash
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.modia.client.unleash.FIKS_CACHE_ENABLED
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.logging.AuditService
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.responses.ok_digisossak_response_string
import no.nav.sosialhjelp.modia.responses.ok_minimal_jsondigisossoker_response_string
import no.nav.sosialhjelp.modia.service.idporten.IdPortenService
import no.nav.sosialhjelp.modia.utils.RequestUtils
import no.nav.sosialhjelp.modia.utils.objectMapper
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
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
    private val unleash: Unleash = mockk()

    private val fiksClient = FiksClientImpl(clientProperties, restTemplate, idPortenService, auditService, redisService, unleash)

    private val id = "123"

    @BeforeEach
    fun init() {
        clearAllMocks()

        mockkObject(RequestUtils)
        every { RequestUtils.getSosialhjelpModiaSessionId() } returns "abcdefghijkl"

        every { idPortenService.getToken().token } returns "token"
        every { auditService.reportFiks(any(), any(), any(), any()) } just Runs

        every { redisService.get(any(), any()) } returns null
        every { redisService.set(any(), any(), any()) } just Runs
        every { redisService.defaultTimeToLiveSeconds } returns 1

        every { unleash.isEnabled(FIKS_CACHE_ENABLED, false) } returns true
    }

    @AfterEach
    internal fun tearDown() {
        unmockkObject(RequestUtils)
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
        verify(exactly = 0) { redisService.set(any(), any(), any()) }
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
        verify(exactly = 1) { redisService.set(any(), any(), any()) }

        every { redisService.get(any(), DigisosSak::class.java) } returns digisosSak

        val result = fiksClient.hentDigisosSak(id)

        assertThat(result).isNotNull
        verify(exactly = 2) { redisService.get(any(), any()) }
        verify(exactly = 1) { restTemplate.exchange(any(), any(), any(), DigisosSak::class.java, any()) }
        verify(exactly = 1) { redisService.set(any(), any(), any()) }
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
        verify(exactly = 1) { redisService.set(any(), any(), any()) }
    }

    @Test
    fun `skal ikke bruke cache hvis sessionId er null`() {
        every { RequestUtils.getSosialhjelpModiaSessionId() } returns null

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

        verify(exactly = 0) { redisService.get(any(), any()) }
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
        verify(exactly = 0) { redisService.set(any(), any(), any()) }
    }
}