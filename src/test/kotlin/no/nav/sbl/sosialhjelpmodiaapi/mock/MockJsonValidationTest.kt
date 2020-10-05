package no.nav.sbl.sosialhjelpmodiaapi.mock

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.client.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.digisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.service.innsyn.InnsynService
import no.nav.sosialhjelp.api.fiks.DigisosSak
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
        every { mockDigisosSak.fiksDigisosId } returns "123"
        every { mockDigisosSak.sokerFnr } returns "11111111111"
        every { mockDigisosSak.originalSoknadNAV?.timestampSendt } returns 1L
        every { mockDigisosSak.digisosSoker?.metadata } returns "some id"
        every { mockDigisosSak.tilleggsinformasjon?.enhetsnummer } returns "9999"
        every { norgClient.hentNavEnhet("9999")!!.navn } returns "NAV test"

        assertThatCode { eventService.createModel(mockDigisosSak) }.doesNotThrowAnyException()
    }
}