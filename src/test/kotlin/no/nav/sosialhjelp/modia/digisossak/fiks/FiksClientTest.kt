package no.nav.sosialhjelp.modia.digisossak.fiks

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import no.finn.unleash.Unleash
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.exceptions.FiksNotFoundException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksServerException
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.app.exceptions.ManglendeTilgangException
import no.nav.sosialhjelp.modia.app.maskinporten.MaskinportenClient
import no.nav.sosialhjelp.modia.client.unleash.BERGEN_ENABLED
import no.nav.sosialhjelp.modia.client.unleash.FIKS_CACHE_ENABLED
import no.nav.sosialhjelp.modia.client.unleash.STAVANGER_ENABLED
import no.nav.sosialhjelp.modia.logging.AuditService
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.responses.ok_digisossak_annen_kommune_response_string
import no.nav.sosialhjelp.modia.responses.ok_digisossak_response_string
import no.nav.sosialhjelp.modia.responses.ok_minimal_jsondigisossoker_response_string
import no.nav.sosialhjelp.modia.utils.RequestUtils
import no.nav.sosialhjelp.modia.utils.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime

internal class FiksClientTest {

    private val mockWebServer = MockWebServer()
    private val fiksWebClient = WebClient.create(mockWebServer.url("/").toString())
    private val clientProperties: ClientProperties = mockk(relaxed = true)
    private val maskinportenClient: MaskinportenClient = mockk()
    private val auditService: AuditService = mockk()
    private val redisService: RedisService = mockk()
    private val unleash: Unleash = mockk()
    private val retryProperties: FiksRetryProperties = mockk()

    private val fiksClient = FiksClientImpl(fiksWebClient, clientProperties, maskinportenClient, auditService, redisService, unleash, retryProperties)

    private val id = "123"

    @BeforeEach
    fun init() {
        clearAllMocks()
        mockWebServer.start()

        mockkObject(RequestUtils)
        every { RequestUtils.getSosialhjelpModiaSessionId() } returns "abcdefghijkl"

        every { maskinportenClient.getToken() } returns "token"
        every { auditService.reportFiks(any(), any(), any(), any()) } just Runs

        every { redisService.get(any(), any(), any()) } returns null
        every { redisService.set(any(), any(), any(), any()) } just Runs
        every { redisService.defaultTimeToLiveSeconds } returns 1

        every { retryProperties.attempts } returns 2
        every { retryProperties.initialDelay } returns 5
        every { retryProperties.maxDelay } returns 10

        every { clientProperties.bergenKommunenummer } returns "1234"
        every { clientProperties.stavangerKommunenummer } returns "1111"
        every { unleash.isEnabled(FIKS_CACHE_ENABLED, false) } returns true
        every { unleash.isEnabled(BERGEN_ENABLED, false) } returns true
        every { unleash.isEnabled(STAVANGER_ENABLED, false) } returns true
    }

    @AfterEach
    internal fun tearDown() {
        unmockkObject(RequestUtils)
        mockWebServer.shutdown()
    }

    @Test
    fun `GET hent alle DigisosSaker`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(
                    listOf(
                        ok_digisossak_response_string(),
                        ok_digisossak_response_string(LocalDateTime.now().minusMonths(1)),
                        ok_digisossak_response_string(LocalDateTime.now().minusMonths(2)),
                    ).toString()
                )
        )

        val result = fiksClient.hentAlleDigisosSaker(id)

        assertThat(result).isNotNull
        assertThat(result).hasSize(3)
    }

    @Test
    fun `GET hent alle DigisosSaker bortsett fra de eldre enn 15 maneder`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(
                    listOf(
                        ok_digisossak_response_string(),
                        ok_digisossak_response_string(LocalDateTime.now().minusMonths(1)),
                        ok_digisossak_response_string(LocalDateTime.now().minusMonths(16)),
                    ).toString()
                )
        )

        val result = fiksClient.hentAlleDigisosSaker(id)

        assertThat(result).isNotNull
        assertThat(result).hasSize(2)
    }

    @Test
    fun `GET eksakt 1 DigisosSak`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(ok_digisossak_response_string())
        )

        val result = fiksClient.hentDigisosSak(id)

        assertThat(result).isNotNull
    }

    @Test
    fun `GET eksakt 0 DigisosSak dersom den er eldre enn 15 maneder`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(ok_digisossak_response_string(LocalDateTime.now().minusMonths(16)))
        )

        assertThatExceptionOfType(FiksNotFoundException::class.java)
            .isThrownBy { fiksClient.hentDigisosSak(id) }
    }

    @Test
    fun `GET feiler dersom kummunen ikke er riktig`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(ok_digisossak_annen_kommune_response_string)
        )

        assertThatExceptionOfType(ManglendeTilgangException::class.java)
            .isThrownBy { fiksClient.hentDigisosSak(id) }
    }

    @Test
    fun `GET DigisosSak feiler hvis Fiks gir 500`() {
        every { retryProperties.attempts } returns 1

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
        )

        assertThatExceptionOfType(FiksServerException::class.java)
            .isThrownBy { fiksClient.hentDigisosSak(id) }
    }

    @Test
    fun `GET digisosSak fra cache`() {
        val digisosSak: DigisosSak = objectMapper.readValue(ok_digisossak_response_string())
        every { redisService.get(any(), any(), DigisosSak::class.java) } returns digisosSak

        val result2 = fiksClient.hentDigisosSak(id)

        assertThat(result2).isNotNull

        verify(exactly = 0) { redisService.set(any(), any(), any()) }
    }

    @Test
    fun `GET digisosSak fra cache etter put`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(ok_digisossak_response_string())
        )

        val result1 = fiksClient.hentDigisosSak(id)

        assertThat(result1).isNotNull
        verify(exactly = 1) { redisService.get(any(), any(), DigisosSak::class.java) }
        verify(exactly = 1) { redisService.set(any(), any(), any(), any()) }

        val digisosSak: DigisosSak = objectMapper.readValue(ok_digisossak_response_string())
        every { redisService.get(any(), any(), DigisosSak::class.java) } returns digisosSak

        val result = fiksClient.hentDigisosSak(id)

        assertThat(result).isNotNull
        verify(exactly = 2) { redisService.get(any(), any(), any()) }
        verify(exactly = 1) { redisService.set(any(), any(), any(), any()) }
    }

    @Test
    fun `GET digisosSak fra annen saksbehandler`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(ok_digisossak_response_string())
        )

        val digisosSak = objectMapper.readValue(ok_digisossak_response_string(), DigisosSak::class.java)

//        annen veileder har hentet digisosSak tidligere (og finnes i cache)
        val annenSaksbehandler = "other"
        every { redisService.get(any(), "${annenSaksbehandler}_$id", DigisosSak::class.java) } returns digisosSak

        val result = fiksClient.hentDigisosSak(id)

        assertThat(result).isNotNull
        verify(exactly = 1) { redisService.get(any(), any(), any()) }
        verify(exactly = 1) { redisService.set(any(), any(), any(), any()) }
    }

    @Test
    fun `skal ikke bruke cache hvis sessionId er null`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(ok_digisossak_response_string())
        )

        every { RequestUtils.getSosialhjelpModiaSessionId() } returns null

        val result = fiksClient.hentDigisosSak(id)

        assertThat(result).isNotNull

        verify(exactly = 0) { redisService.get(any(), any(), any()) }
    }

    @Test
    fun `GET dokument`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(ok_minimal_jsondigisossoker_response_string)
        )

        val result = fiksClient.hentDokument("fnr", id, "dokumentlagerId", JsonDigisosSoker::class.java)

        assertNotNull(result)
    }

    @Test
    fun `GET dokument fra cache`() {
        val jsonDigisosSoker: JsonDigisosSoker = objectMapper.readValue(ok_minimal_jsondigisossoker_response_string)
        every { redisService.get(any(), any(), JsonDigisosSoker::class.java) } returns jsonDigisosSoker

        val result = fiksClient.hentDokument("fnr", id, "dokumentlagerId", JsonDigisosSoker::class.java)

        assertNotNull(result)
        verify(exactly = 0) { redisService.set(any(), any(), any()) }
    }
}
