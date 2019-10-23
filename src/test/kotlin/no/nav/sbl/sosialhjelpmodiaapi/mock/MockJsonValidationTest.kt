package no.nav.sbl.sosialhjelpmodiaapi.mock

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.innsyn.InnsynService
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.digisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.norg.NorgClient
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

internal class DefaultMockResponseTest {

    private val innsynService: InnsynService = mockk()
    private val clientProperties: ClientProperties = mockk(relaxed = true)
    private val norgClient: NorgClient = mockk(relaxed = true)
    private val fiksClient: FiksClient = mockk(relaxed = true)

    private val eventService = EventService(clientProperties, innsynService, norgClient, fiksClient)

    @Test
    fun `validerer digisosSoker`() {
        every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns digisosSoker
        every { innsynService.hentOriginalSoknad(any(), any(), any()) } returns null
        every { fiksClient.hentDigisosSak(any(), any()).originalSoknadNAV?.timestampSendt } returns 1L

        assertThatCode { eventService.createModel("123", "token") }.doesNotThrowAnyException()
    }
}