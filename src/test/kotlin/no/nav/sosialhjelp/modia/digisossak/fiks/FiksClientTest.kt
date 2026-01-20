package no.nav.sosialhjelp.modia.digisossak.fiks

import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.exceptions.FiksNotFoundException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksServerException
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.app.maskinporten.MaskinportenClient
import no.nav.sosialhjelp.modia.logging.AuditService
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.responses.okDigisossakResponseString
import no.nav.sosialhjelp.modia.responses.ok_minimal_jsondigisossoker_response_string
import no.nav.sosialhjelp.modia.utils.RequestUtils
import no.nav.sosialhjelp.modia.utils.sosialhjelpJsonMapper
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
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
import tools.jackson.module.kotlin.readValue

internal class FiksClientTest {
    private val mockWebServer = MockWebServer()
    private val fiksWebClient = WebClient.create(mockWebServer.url("/").toString())
    private val clientProperties: ClientProperties = mockk(relaxed = true)
    private val maskinportenClient: MaskinportenClient = mockk()
    private val auditService: AuditService = mockk()
    private val redisService: RedisService = mockk()
    private val maxRetryAttempts = 2L
    private val initialDelay = 2L
    private val documentTTL = 360L

    private val fiksClient =
        FiksClientImpl(
            fiksWebClient = fiksWebClient,
            clientProperties = clientProperties,
            maskinportenClient = maskinportenClient,
            auditService = auditService,
            redisService = redisService,
            maxAttempts = maxRetryAttempts,
            initialDelay = initialDelay,
            documentTTL = documentTTL,
        )

    private val id = "123"

    @BeforeEach
    fun init() {
        clearAllMocks()

        mockkObject(RequestUtils)
        every { RequestUtils.getSosialhjelpModiaSessionId() } returns "abcdefghijkl"

        every { maskinportenClient.getToken() } returns "token"
        every { auditService.reportFiks(any(), any(), any(), any()) } just Runs

        every { redisService.get<Any>(any(), any(), any()) } returns null
        every { redisService.set(any(), any(), any(), any()) } just Runs
        every { redisService.defaultTimeToLiveSeconds } returns 1
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
                        okDigisossakResponseString(),
                        okDigisossakResponseString(LocalDateTime.now().minusMonths(1)),
                        okDigisossakResponseString(LocalDateTime.now().minusMonths(2)),
                    ).toString(),
                ),
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
                        okDigisossakResponseString(),
                        okDigisossakResponseString(LocalDateTime.now().minusMonths(1)),
                        okDigisossakResponseString(LocalDateTime.now().minusMonths(16)),
                    ).toString(),
                ),
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
                .setBody(okDigisossakResponseString()),
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
                .setBody(okDigisossakResponseString(LocalDateTime.now().minusMonths(16))),
        )

        assertThatExceptionOfType(FiksNotFoundException::class.java)
            .isThrownBy { fiksClient.hentDigisosSak(id) }
    }

    @Test
    fun `GET DigisosSak skal bruke retry hvis Fiks gir 5xx-feil`() {
        repeat(maxRetryAttempts.toInt() + 1) {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(500),
            )
        }

        assertThatExceptionOfType(FiksServerException::class.java)
            .isThrownBy { fiksClient.hentDigisosSak(id) }
        assertThat(mockWebServer.requestCount).isEqualTo(maxRetryAttempts + 1)
    }

    @Test
    fun `GET digisosSak fra cache`() {
        val digisosSak: DigisosSak = sosialhjelpJsonMapper.readValue(okDigisossakResponseString())
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
                .setBody(okDigisossakResponseString()),
        )

        val result1 = fiksClient.hentDigisosSak(id)

        assertThat(result1).isNotNull
        verify(exactly = 1) { redisService.get(any(), any(), DigisosSak::class.java) }
        verify(exactly = 1) { redisService.set(any(), any(), any(), any()) }

        val digisosSak: DigisosSak = sosialhjelpJsonMapper.readValue(okDigisossakResponseString())
        every { redisService.get(any(), any(), DigisosSak::class.java) } returns digisosSak

        val result = fiksClient.hentDigisosSak(id)

        assertThat(result).isNotNull
        verify(exactly = 2) { redisService.get<DigisosSak>(any(), any(), any()) }
        verify(exactly = 1) { redisService.set(any(), any(), any(), any()) }
    }

    @Test
    fun `GET digisosSak fra annen saksbehandler`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(okDigisossakResponseString()),
        )

        val digisosSak = sosialhjelpJsonMapper.readValue(okDigisossakResponseString(), DigisosSak::class.java)

//        annen veileder har hentet digisosSak tidligere (og finnes i cache)
        val annenSaksbehandler = "other"
        every { redisService.get(any(), "${annenSaksbehandler}_$id", DigisosSak::class.java) } returns digisosSak

        val result = fiksClient.hentDigisosSak(id)

        assertThat(result).isNotNull
        verify(exactly = 1) { redisService.get<DigisosSak>(any(), any(), any()) }
        verify(exactly = 1) { redisService.set(any(), any(), any(), any()) }
    }

    @Test
    fun `skal ikke bruke cache hvis sessionId er null`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(okDigisossakResponseString()),
        )

        every { RequestUtils.getSosialhjelpModiaSessionId() } returns null

        val result = fiksClient.hentDigisosSak(id)

        assertThat(result).isNotNull

        verify(exactly = 0) { redisService.get<DigisosSak>(any(), any(), any()) }
    }

    @Test
    fun `GET dokument`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(ok_minimal_jsondigisossoker_response_string),
        )

        val result = fiksClient.hentDokument("fnr", id, "dokumentlagerId", JsonDigisosSoker::class.java)

        assertNotNull(result)
    }

    @Test
    fun `GET dokument fra cache`() {
        val jsonDigisosSoker: JsonDigisosSoker = sosialhjelpJsonMapper.readValue(ok_minimal_jsondigisossoker_response_string)
        every { redisService.get(any(), any(), JsonDigisosSoker::class.java) } returns jsonDigisosSoker

        val result = fiksClient.hentDokument("fnr", id, "dokumentlagerId", JsonDigisosSoker::class.java)

        assertNotNull(result)
        verify(exactly = 0) { redisService.set(any(), any(), any()) }
    }
}
