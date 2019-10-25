package no.nav.sbl.sosialhjelpmodiaapi.mock

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.domain.DigisosSak
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.innsyn.InnsynService
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.digisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.norg.NorgClient
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

internal class DefaultMockResponseTest {

    private val innsynService: InnsynService = mockk()
    private val norgClient: NorgClient = mockk(relaxed = true)

    private val eventService = EventService(innsynService, norgClient)

    @Test
    fun `validerer digisosSoker`() {
        val mockDigisosSak: DigisosSak = mockk()
        every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns digisosSoker
        every { innsynService.hentOriginalSoknad(any(), any(), any()) } returns null
        every { mockDigisosSak.fiksDigisosId } returns "123"
        every { mockDigisosSak.originalSoknadNAV?.timestampSendt } returns 1L
        every { mockDigisosSak.digisosSoker?.metadata } returns "some id"
        every { mockDigisosSak.originalSoknadNAV?.metadata } returns "some other id"

        assertThatCode { eventService.createModel(mockDigisosSak, "token") }.doesNotThrowAnyException()
    }
}