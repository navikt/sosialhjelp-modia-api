package no.nav.sosialhjelp.modia.service.soknadsstatus

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.modia.client.fiks.FiksClient
import no.nav.sosialhjelp.modia.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.domain.SoknadsStatus
import no.nav.sosialhjelp.modia.domain.SoknadsStatusResponse
import no.nav.sosialhjelp.modia.event.EventService
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SoknadsStatusServiceTest {

    private val fiksClient: FiksClient = mockk()
    private val eventService: EventService = mockk()

    private val service = SoknadsStatusService(fiksClient, eventService)

    private val mockDigisosSak: DigisosSak = mockk()
    private val mockInternalDigisosSoker: InternalDigisosSoker = mockk()

    @BeforeEach
    fun init() {
        clearAllMocks()
        every { fiksClient.hentDigisosSak(any()) } returns mockDigisosSak
    }

    @Test
    fun `Skal returnere mest nylige SoknadsStatus`() {
        every { eventService.createModel(any()) } returns mockInternalDigisosSoker
        every { mockInternalDigisosSoker.status } returns SoknadsStatus.UNDER_BEHANDLING

        val response: SoknadsStatusResponse = service.hentSoknadsStatus("123")

        assertThat(response).isNotNull
        assertThat(response.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
    }
}