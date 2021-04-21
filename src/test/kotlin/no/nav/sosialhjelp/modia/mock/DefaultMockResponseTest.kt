package no.nav.sosialhjelp.modia.mock

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.modia.client.norg.NorgClient
import no.nav.sosialhjelp.modia.event.EventService
import no.nav.sosialhjelp.modia.mock.responses.digisosSoker
import no.nav.sosialhjelp.modia.service.innsyn.InnsynService
import no.nav.sosialhjelp.modia.service.vedlegg.SoknadVedleggService
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

internal class DefaultMockResponseTest {

    private val innsynService: InnsynService = mockk()
    private val norgClient: NorgClient = mockk(relaxed = true)
    private val soknadVedleggService: SoknadVedleggService = mockk()

    private val eventService = EventService(innsynService, norgClient, soknadVedleggService)

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
