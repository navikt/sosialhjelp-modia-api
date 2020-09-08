package no.nav.sbl.sosialhjelpmodiaapi.service.kommune

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.sbl.sosialhjelpmodiaapi.redis.RedisService
import no.nav.sbl.sosialhjelpmodiaapi.responses.ok_kommuneinfo_response_string
import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KommuneServiceTest {

    private val kommuneInfoClient: KommuneInfoClient = mockk()
    private val redisService: RedisService = mockk()

    private val service = KommuneService(kommuneInfoClient, redisService)

    private val mockDigisosSak: DigisosSak = mockk()
    private val kommuneNr = "1234"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { mockDigisosSak.kommunenummer } returns kommuneNr

        every { redisService.get(any(), any()) } returns null
        every { redisService.put(any(), any()) } just Runs
    }

    @Test
    internal fun `hent KommuneInfo fra cache`() {
        val kommuneInfo: KommuneInfo = objectMapper.readValue(ok_kommuneinfo_response_string)
        every { redisService.get(any(), any()) } returns kommuneInfo

        val result = service.get(kommuneNr)

        assertThat(result).isNotNull
        verify(exactly = 1) { redisService.get(any(), any()) }
        verify(exactly = 0) { kommuneInfoClient.get(any()) }
        verify(exactly = 0) { redisService.put(any(), any()) }
    }

    @Test
    internal fun `hent KommuneInfo fra client`() {
        every { kommuneInfoClient.get(any()) } returns objectMapper.readValue(ok_kommuneinfo_response_string)

        val result = service.get(kommuneNr)

        assertThat(result).isNotNull
        verify(exactly = 1) { redisService.get(any(), any()) }
        verify(exactly = 1) { kommuneInfoClient.get(any()) }
        verify(exactly = 1) { redisService.put(any(), any()) }
    }
}