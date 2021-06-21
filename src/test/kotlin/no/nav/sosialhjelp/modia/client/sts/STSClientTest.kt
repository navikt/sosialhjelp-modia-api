package no.nav.sosialhjelp.modia.client.sts

import io.mockk.clearAllMocks
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import no.nav.sosialhjelp.modia.utils.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

internal class STSClientTest {

    private val mockWebServer = MockWebServer()
    private val stsWebClient: WebClient = WebClient.create(mockWebServer.url("/").toString())

    private val stsClient = STSClient(stsWebClient)

    @BeforeEach
    fun init() {
        clearAllMocks()
        mockWebServer.start()
    }

    @AfterEach
    internal fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `GET STSToken`() {
        val token = STSToken("access_token", "type", 1234L)

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(token))
        )

        val accessToken = stsClient.token()

        assertThat(accessToken).isNotNull
        assertThat(accessToken).isEqualTo(token.access_token)
    }

    @Test
    fun `GET STSToken - skal bruke cache hvis cachedToken er satt og fortsatt gyldig`() {
        val token = STSToken("access_token", "type", 3600L)

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(token))
        )

        val firstResponse = stsClient.token()

        assertThat(firstResponse).isNotNull
        assertThat(firstResponse).isEqualTo(token.access_token)

        val token2 = STSToken("access_token2", "type2", 2000L)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(token2))
        )

        val secondResponse = stsClient.token()

        assertThat(secondResponse).isEqualTo(token.access_token)
    }

    @Test
    fun `GET STSToken - skal ikke bruke cache hvis cachedToken er satt men utgaatt`() {
        // sett cachedToken til et token med under 10s levetid:
        val token = STSToken("access_token1", "type", 5L)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(token))
        )

        val firstToken = stsClient.token()

        assertThat(firstToken).isNotNull
        assertThat(firstToken).isEqualTo(token.access_token)

        val token2 = STSToken("access_token2", "type", 3600L) // under grensen på 10s for utgått token

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(token2))
        )

        val secondToken = stsClient.token()

        assertThat(secondToken).isNotNull
        assertThat(secondToken).isEqualTo(token2.access_token)
    }
}
