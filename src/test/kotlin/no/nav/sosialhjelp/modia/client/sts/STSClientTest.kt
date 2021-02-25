package no.nav.sosialhjelp.modia.client.sts

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.sosialhjelp.modia.config.ClientProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import kotlin.test.assertNotNull

internal class STSClientTest {

    private val clientProperties: ClientProperties = mockk(relaxed = true)
    private val restTemplate: RestTemplate = mockk()

    private val stsClient = STSClient(restTemplate, clientProperties)

    @BeforeEach
    fun init() {
        clearAllMocks()
    }

    @Test
    fun `GET STSToken`() {
        val response: ResponseEntity<STSToken> = mockk()
        val token = STSToken("access_token", "type", 1234L)

        every { response.body } returns token
        every {
            restTemplate.exchange(
                    any<String>(),
                    HttpMethod.POST,
                    any(),
                    STSToken::class.java)
        } returns response

        val accessToken = stsClient.token()

        assertNotNull(accessToken)
        assertThat(accessToken).isEqualTo(token.access_token)
    }

    @Test
    fun `GET STSToken - skal bruke cache hvis cachedToken er satt og fortsatt gyldig`() {
        val response: ResponseEntity<STSToken> = mockk()
        val token = STSToken("access_token", "type", 3600L)

        every { response.body } returns token
        every {
            restTemplate.exchange(
                    any<String>(),
                    HttpMethod.POST,
                    any(),
                    STSToken::class.java)
        } returns response

        val firstToken = stsClient.token()

        assertNotNull(firstToken)
        assertThat(firstToken).isEqualTo(token.access_token)
        verify(exactly = 1) { restTemplate.exchange(any<String>(), HttpMethod.POST, any(), STSToken::class.java) }

        val secondToken = stsClient.token()

        assertThat(secondToken).isEqualTo(token.access_token)
        verify(exactly = 1) { restTemplate.exchange(any<String>(), HttpMethod.POST, any(), STSToken::class.java) }
    }

    @Test
    fun `GET STSToken - skal ikke bruke cache hvis cachedToken er satt men utgaatt`() {
        val response: ResponseEntity<STSToken> = mockk()
        val token = STSToken("access_token", "type", 5L) // under grensen på 10s for utgått token

        every { response.body } returns token
        every {
            restTemplate.exchange(
                    any<String>(),
                    HttpMethod.POST,
                    any(),
                    STSToken::class.java)
        } returns response

        val firstToken = stsClient.token()

        assertNotNull(firstToken)
        verify(exactly = 1) { restTemplate.exchange(any<String>(), HttpMethod.POST, any(), STSToken::class.java) }

        val secondToken = stsClient.token()

        assertNotNull(secondToken)
        verify(exactly = 2) { restTemplate.exchange(any<String>(), HttpMethod.POST, any(), STSToken::class.java) }
    }
}