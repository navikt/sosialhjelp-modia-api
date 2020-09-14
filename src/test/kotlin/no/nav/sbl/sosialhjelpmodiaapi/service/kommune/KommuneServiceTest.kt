package no.nav.sbl.sosialhjelpmodiaapi.service.kommune

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import org.junit.jupiter.api.BeforeEach

internal class KommuneServiceTest {

    private val kommuneInfoClient: KommuneInfoClient = mockk()

    private val service = KommuneService(kommuneInfoClient)

    private val mockDigisosSak: DigisosSak = mockk()
    private val kommuneNr = "1234"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { mockDigisosSak.kommunenummer } returns kommuneNr
    }

}