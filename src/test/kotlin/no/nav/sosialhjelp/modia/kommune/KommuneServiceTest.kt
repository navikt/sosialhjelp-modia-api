package no.nav.sosialhjelp.modia.kommune

import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.modia.kommune.fiks.KommuneInfoClient
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.responses.kommuneInfoResponseString
import no.nav.sosialhjelp.modia.utils.sosialhjelpJsonMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.readValue

internal class KommuneServiceTest {
    private val kommuneInfoClient: KommuneInfoClient = mockk()
    private val redisService: RedisService = mockk()

    private val service = KommuneService(kommuneInfoClient, redisService)

    private val mockDigisosSak: DigisosSak = mockk()
    private val kommuneNr = "1234"
    private val kommunenavnUtenKommuneINavnet = "Nabonavn"
    private val kommunenavnMedKommuneINavnet = "Naboen kommune"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { mockDigisosSak.kommunenummer } returns kommuneNr
        every { redisService.get<Any>(any(), any(), any()) } returns null
        every { redisService.set(any(), any(), any(), any()) } just Runs
        every { redisService.defaultTimeToLiveSeconds } returns 1
    }

    @Test
    internal suspend fun `hent KommuneInfo fra cache`() {
        val kommuneInfo: KommuneInfo = sosialhjelpJsonMapper.readValue(kommuneInfoResponseString)
        every { redisService.get<KommuneInfo>(any(), any(), any()) } returns kommuneInfo

        val result = service.get(kommuneNr)

        assertThat(result).isNotNull
        verify(exactly = 1) { redisService.get<KommuneInfo>(any(), any(), any()) }
        coVerify(exactly = 0) { kommuneInfoClient.getKommuneInfo(any()) }
        verify(exactly = 0) { redisService.set(any(), any(), any(), any()) }
    }

    @Test
    internal suspend fun `hent KommuneInfo fra client`() {
        coEvery { kommuneInfoClient.getKommuneInfo(any()) } returns
            sosialhjelpJsonMapper.readValue(kommuneInfoResponseString)

        val result = service.get(kommuneNr)

        assertThat(result).isNotNull
        verify(exactly = 1) { redisService.get<KommuneInfo>(any(), any(), any()) }
        coVerify(exactly = 1) { kommuneInfoClient.getKommuneInfo(any()) }
        verify(exactly = 1) { redisService.set(any(), any(), any(), any()) }
    }

    @Test
    suspend fun `behandlingsansvarlig returneres med kommune i kommunenavnet det ikke finnes i kommune info`() {
        val kommuneInfo = KommuneInfo("", true, true, false, false, null, true, kommunenavnUtenKommuneINavnet)
        coEvery { kommuneInfoClient.getKommuneInfo(kommuneNr) } returns kommuneInfo

        val behandlingsansvarlig = service.getBehandlingsanvarligKommune(kommuneNr)
        assertThat(behandlingsansvarlig).isEqualTo("$kommunenavnUtenKommuneINavnet kommune")
    }

    @Test
    suspend fun `behandlingsansvarlig med kommune i kommunenavnet returneres med kommune i navnet`() {
        val kommuneInfo = KommuneInfo("", true, true, false, false, null, true, kommunenavnMedKommuneINavnet)
        coEvery { kommuneInfoClient.getKommuneInfo(kommuneNr) } returns kommuneInfo

        val behandlingsansvarlig = service.getBehandlingsanvarligKommune(kommuneNr)
        assertThat(behandlingsansvarlig).isEqualTo(kommunenavnMedKommuneINavnet)
    }

    @Test
    suspend fun `ingen behandlinsansvarlig satt returnerer null`() {
        val kommuneInfo = KommuneInfo("", true, true, false, false, null, true, null)
        coEvery { kommuneInfoClient.getKommuneInfo(kommuneNr) } returns kommuneInfo

        val behandlingsansvarlig = service.getBehandlingsanvarligKommune(kommuneNr)
        assertThat(behandlingsansvarlig).isNull()
    }
}
