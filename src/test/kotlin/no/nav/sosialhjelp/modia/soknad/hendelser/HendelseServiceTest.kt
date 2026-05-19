package no.nav.sosialhjelp.modia.soknad.hendelser

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.modia.digisossak.domain.Hendelse
import no.nav.sosialhjelp.modia.digisossak.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.event.EventService
import no.nav.sosialhjelp.modia.digisossak.event.Titler.SOKNAD_MOTTATT
import no.nav.sosialhjelp.modia.digisossak.event.Titler.SOKNAD_SENDT
import no.nav.sosialhjelp.modia.digisossak.event.Titler.SOKNAD_UNDER_BEHANDLING
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksClient
import no.nav.sosialhjelp.modia.soknad.vedlegg.VedleggService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

internal class HendelseServiceTest {
    private val fiksClient: FiksClient = mockk()
    private val eventService: EventService = mockk()
    private val vedleggsService: VedleggService = mockk()
    private val service = HendelseService(fiksClient, eventService, vedleggsService)

    private val mockDigisosSak: DigisosSak = mockk()

    private val tidspunktSendt = LocalDateTime.now().minusDays(1)
    private val tidspunktMottatt = LocalDateTime.now().minusHours(10)
    private val tidspunkt3 = LocalDateTime.now().minusHours(9)

    private val tittelSendt = "søknad sendt"
    private val tittelMottatt = "søknad mottatt"
    private val tittel3 = "tittel 3"

    @BeforeEach
    fun init() {
        clearMocks(eventService, fiksClient)

        coEvery { fiksClient.hentDigisosSak(any()) } returns mockDigisosSak
        coEvery { vedleggsService.hentEttersendteVedlegg(any(), any()) } returns emptyList()
        every { mockDigisosSak.ettersendtInfoNAV } returns mockk()
        every { mockDigisosSak.originalSoknadNAV?.timestampSendt } returns tidspunktSendt.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    @Test
    suspend fun `Skal returnere respons med 1 hendelse`() {
        val model = InternalDigisosSoker()
        model.historikk.add(Hendelse(SOKNAD_SENDT, tittelSendt, tidspunktSendt))

        coEvery { eventService.createModel(any()) } returns model

        val hendelser = service.hentHendelser("123")

        assertThat(hendelser).hasSize(1)
        assertThat(hendelser[0].beskrivelse).isEqualTo(tittelSendt)
        assertThat(hendelser[0].tidspunkt).isEqualTo(tidspunktSendt.toString())
    }

    @Test
    suspend fun `Skal returnere respons med flere hendelser`() {
        val model = InternalDigisosSoker()
        model.historikk.addAll(
            listOf(
                Hendelse(SOKNAD_SENDT, tittelSendt, tidspunktSendt),
                Hendelse(SOKNAD_MOTTATT, tittelMottatt, tidspunktMottatt),
                Hendelse(SOKNAD_UNDER_BEHANDLING, tittel3, tidspunkt3),
            ),
        )

        coEvery { eventService.createModel(any()) } returns model

        val hendelser = service.hentHendelser("123")

        assertThat(hendelser).hasSize(3)
    }
}
