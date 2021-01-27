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
import no.nav.sbl.sosialhjelpmodiaapi.service.idporten.IdPortenService
import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KommuneServiceTest {

    private val kommuneInfoClient: KommuneInfoClient = mockk()
    private val idPortenService: IdPortenService = mockk()
    private val redisService: RedisService = mockk()

    private val service = KommuneService(kommuneInfoClient, idPortenService, redisService)

    private val mockDigisosSak: DigisosSak = mockk()
    private val kommuneNr = "1234"
    private val kommunenavnUtenKommuneINavnet = "Nabonavn"
    private val kommunenavnMedKommuneINavnet = "Naboen kommune"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { mockDigisosSak.kommunenummer } returns kommuneNr
        every { idPortenService.getToken().token } returns "tokentoken"
        every { redisService.get(any(), any()) } returns null
        every { redisService.set(any(), any(), any()) } just Runs
        every { redisService.defaultTimeToLiveSeconds } returns 1
    }

    @Test
    internal fun `hent KommuneInfo fra cache`() {
        val kommuneInfo: KommuneInfo = objectMapper.readValue(ok_kommuneinfo_response_string)
        every { redisService.get(any(), any()) } returns kommuneInfo

        val result = service.get(kommuneNr)

        assertThat(result).isNotNull
        verify(exactly = 1) { redisService.get(any(), any()) }
        verify(exactly = 0) { kommuneInfoClient.get(any(), any()) }
        verify(exactly = 0) { redisService.set(any(), any(), any()) }
    }

    @Test
    internal fun `hent KommuneInfo fra client`() {
        every { kommuneInfoClient.get(any(), any()) } returns objectMapper.readValue(ok_kommuneinfo_response_string)

        val result = service.get(kommuneNr)

        assertThat(result).isNotNull
        verify(exactly = 1) { redisService.get(any(), any()) }
        verify(exactly = 1) { kommuneInfoClient.get(any(), any()) }
        verify(exactly = 1) { redisService.set(any(), any(), any()) }
    }

    @Test
    fun `behandlingsansvarlig returneres med kommune i kommunenavnet det ikke finnes fra før satt`() {
        val kommuneInfo = KommuneInfo("", true, true,false, false, null, true, kommunenavnUtenKommuneINavnet)
        every { kommuneInfoClient.get(kommuneNr, any()) } returns kommuneInfo

        val behandlingsansvarlig = service.getBehandlingsanvarligKommune(kommuneNr)
        assertThat(behandlingsansvarlig).isEqualTo("$kommunenavnUtenKommuneINavnet kommune")
    }

    @Test
    fun `behandlingsansvarlig med kommune i kommunenavnet returneres med kommune i navnet`() {
        val kommuneInfo = KommuneInfo("", true, true,false, false, null, true, kommunenavnMedKommuneINavnet)
        every { kommuneInfoClient.get(kommuneNr, any()) } returns kommuneInfo

        val behandlingsansvarlig = service.getBehandlingsanvarligKommune(kommuneNr)
        assertThat(behandlingsansvarlig).isEqualTo(kommunenavnMedKommuneINavnet)
    }

    @Test
    fun `ingen behandlinsansvarlig satt returnerer null`() {
        val kommuneInfo = KommuneInfo("", true, true,false, false, null, true, null)
        every { kommuneInfoClient.get(kommuneNr, any()) } returns kommuneInfo

        val behandlingsansvarlig = service.getBehandlingsanvarligKommune(kommuneNr)
        assertThat(behandlingsansvarlig).isNull()
    }
}