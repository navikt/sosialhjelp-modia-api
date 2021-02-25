package no.nav.sosialhjelp.modia.service.hendelse

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.modia.client.fiks.FiksClient
import no.nav.sosialhjelp.modia.domain.Hendelse
import no.nav.sosialhjelp.modia.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.event.EventService
import no.nav.sosialhjelp.modia.event.Titler.SOKNAD_MOTTATT
import no.nav.sosialhjelp.modia.event.Titler.SOKNAD_SENDT
import no.nav.sosialhjelp.modia.event.Titler.SOKNAD_UNDER_BEHANDLING
import no.nav.sosialhjelp.modia.service.vedlegg.VedleggService
import no.nav.sosialhjelp.api.fiks.DigisosSak
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

    private val tidspunkt_sendt = LocalDateTime.now().minusDays(1)
    private val tidspunkt_mottatt = LocalDateTime.now().minusHours(10)
    private val tidspunkt3 = LocalDateTime.now().minusHours(9)

    private val tittel_sendt = "søknad sendt"
    private val tittel_mottatt = "søknad mottatt"
    private val tittel3 = "tittel 3"

    @BeforeEach
    fun init() {
        clearMocks(eventService, fiksClient)

        every { fiksClient.hentDigisosSak(any()) } returns mockDigisosSak
        every { vedleggsService.hentEttersendteVedlegg(any(), any()) } returns emptyList()
        every { mockDigisosSak.ettersendtInfoNAV } returns mockk()
        every { mockDigisosSak.originalSoknadNAV?.timestampSendt } returns tidspunkt_sendt.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    @Test
    fun `Skal returnere respons med 1 hendelse`() {
        val model = InternalDigisosSoker()
        model.historikk.add(Hendelse(SOKNAD_SENDT, tittel_sendt, tidspunkt_sendt))

        every { eventService.createModel(any()) } returns model

        val hendelser = service.hentHendelser("123")

        assertThat(hendelser).hasSize(1)
        assertThat(hendelser[0].beskrivelse).isEqualTo(tittel_sendt)
        assertThat(hendelser[0].tidspunkt).isEqualTo(tidspunkt_sendt.toString())
    }

    @Test
    fun `Skal returnere respons med flere hendelser`() {
        val model = InternalDigisosSoker()
        model.historikk.addAll(listOf(
                Hendelse(SOKNAD_SENDT, tittel_sendt, tidspunkt_sendt),
                Hendelse(SOKNAD_MOTTATT, tittel_mottatt, tidspunkt_mottatt),
                Hendelse(SOKNAD_UNDER_BEHANDLING, tittel3, tidspunkt3)))

        every { eventService.createModel(any()) } returns model

        val hendelser = service.hentHendelser("123")

        assertThat(hendelser).hasSize(3)
    }
}