package no.nav.sbl.sosialhjelpmodiaapi.service.kommune

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KommuneServiceTest {

    private val kommuneInfoClient: KommuneInfoClient = mockk()

    private val service = KommuneService(kommuneInfoClient)

    private val mockDigisosSak: DigisosSak = mockk()
    private val kommuneNr = "1234"
    private val kommunenavnUtenKommuneINavnet = "Nabonavn"
    private val kommunenavnMedKommuneINavnet = "Naboen kommune"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { mockDigisosSak.kommunenummer } returns kommuneNr
    }

    @Test
    fun `behandlingsansvarlig returneres med kommune i kommunenavnet det ikke finnes fra før satt`() {
        val kommuneInfo = KommuneInfo("", true, true,false, false, null, true, kommunenavnUtenKommuneINavnet)
        every { service.get(kommuneNr) } returns kommuneInfo

        val behandlingsansvarlig = service.getBehandlingsanvarligKommune(kommuneNr)
        assertThat(behandlingsansvarlig).isEqualTo("$kommunenavnUtenKommuneINavnet kommune")
    }

    @Test
    fun `behandlingsansvarlig med kommune i kommunenavnet returneres med kommune i navnet`() {
        val kommuneInfo = KommuneInfo("", true, true,false, false, null, true, kommunenavnMedKommuneINavnet)
        every { service.get(kommuneNr) } returns kommuneInfo

        val behandlingsansvarlig = service.getBehandlingsanvarligKommune(kommuneNr)
        assertThat(behandlingsansvarlig).isEqualTo(kommunenavnMedKommuneINavnet)
    }

    @Test
    fun `ingen behandlinsansvarlig satt returnerer null`() {
        val kommuneInfo = KommuneInfo("", true, true,false, false, null, true, null)
        every { service.get(kommuneNr) } returns kommuneInfo

        val behandlingsansvarlig = service.getBehandlingsanvarligKommune(kommuneNr)
        assertThat(behandlingsansvarlig).isNull()
    }
}