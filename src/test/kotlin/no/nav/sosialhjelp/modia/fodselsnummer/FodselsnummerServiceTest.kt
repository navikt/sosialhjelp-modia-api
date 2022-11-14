package no.nav.sosialhjelp.modia.fodselsnummer

import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.nav.sosialhjelp.modia.redis.RedisService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class FodselsnummerServiceTest {

    private val redisService: RedisService = mockk()
    private val service = FodselsnummerService(redisService)

    private val fnr = "11111111111"
    private val fnrId = "abfcc2e8-9986-48c0-9952-eb6b724df6ce"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        every { redisService.set(any(), any(), any(), any()) } just Runs
        every { redisService.defaultTimeToLiveSeconds } returns 5
    }

    @Test
    internal fun `hent fodselsnummer ved fnrId, returnere fodselsnummer`() {
        every { redisService.get(any(), any(), any()) } returns fnr
        val result = service.getFnr(fnrId)
        assertThat(result).isNotNull
        assertThat(result).isEqualTo(fnr)
    }

    @Test
    internal fun `hent fodselsnummer ved fnrId, skal ikke finnes og returnere null`() {
        every { redisService.get(any(), any(), any()) } returns null
        val result = service.getFnr(fnrId)
        assertThat(result).isNull()
    }
}
