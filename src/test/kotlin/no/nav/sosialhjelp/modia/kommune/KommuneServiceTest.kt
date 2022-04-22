package no.nav.sosialhjelp.modia.kommune

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.modia.kommune.fiks.KommuneInfoClient
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.responses.ok_kommuneinfo_response_string
import no.nav.sosialhjelp.modia.utils.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
        every { redisService.get(any(), any(), any()) } returns null
        every { redisService.set(any(), any(), any(), any()) } just Runs
        every { redisService.defaultTimeToLiveSeconds } returns 1
    }

    @Test
    internal fun `hent KommuneInfo fra cache`() {
        val kommuneInfo: KommuneInfo = objectMapper.readValue(ok_kommuneinfo_response_string)
        every { redisService.get(any(), any(), any()) } returns kommuneInfo

        val result = service.get(kommuneNr)

        assertThat(result).isNotNull
        verify(exactly = 1) { redisService.get(any(), any(), any()) }
        verify(exactly = 0) { kommuneInfoClient.getKommuneInfo(any()) }
        verify(exactly = 0) { redisService.set(any(), any(), any(), any()) }
    }

    @Test
    internal fun `hent KommuneInfo fra client`() {
        every { kommuneInfoClient.getKommuneInfo(any()) } returns objectMapper.readValue(ok_kommuneinfo_response_string)

        val result = service.get(kommuneNr)

        assertThat(result).isNotNull
        verify(exactly = 1) { redisService.get(any(), any(), any()) }
        verify(exactly = 1) { kommuneInfoClient.getKommuneInfo(any()) }
        verify(exactly = 1) { redisService.set(any(), any(), any(), any()) }
    }

    @Test
    fun `behandlingsansvarlig returneres med kommune i kommunenavnet det ikke finnes i kommune info`() {
        val kommuneInfo = KommuneInfo("", true, true, false, false, null, true, kommunenavnUtenKommuneINavnet)
        every { kommuneInfoClient.getKommuneInfo(kommuneNr) } returns kommuneInfo

        val behandlingsansvarlig = service.getBehandlingsanvarligKommune(kommuneNr)
        assertThat(behandlingsansvarlig).isEqualTo("$kommunenavnUtenKommuneINavnet kommune")
    }

    @Test
    fun `behandlingsansvarlig med kommune i kommunenavnet returneres med kommune i navnet`() {
        val kommuneInfo = KommuneInfo("", true, true, false, false, null, true, kommunenavnMedKommuneINavnet)
        every { kommuneInfoClient.getKommuneInfo(kommuneNr) } returns kommuneInfo

        val behandlingsansvarlig = service.getBehandlingsanvarligKommune(kommuneNr)
        assertThat(behandlingsansvarlig).isEqualTo(kommunenavnMedKommuneINavnet)
    }

    @Test
    fun `ingen behandlinsansvarlig satt returnerer null`() {
        val kommuneInfo = KommuneInfo("", true, true, false, false, null, true, null)
        every { kommuneInfoClient.getKommuneInfo(kommuneNr) } returns kommuneInfo

        val behandlingsansvarlig = service.getBehandlingsanvarligKommune(kommuneNr)
        assertThat(behandlingsansvarlig).isNull()
    }
}
