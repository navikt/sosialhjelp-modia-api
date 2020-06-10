package no.nav.sbl.sosialhjelpmodiaapi.service.hendelse

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.event.Titler.SOKNAD_MOTTATT
import no.nav.sbl.sosialhjelpmodiaapi.event.Titler.SOKNAD_SENDT
import no.nav.sbl.sosialhjelpmodiaapi.event.Titler.SOKNAD_UNDER_BEHANDLING
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

internal class HendelseServiceTest {
    private val fiksClient: FiksClient = mockk()
    private val eventService: EventService = mockk()
    private val service = HendelseService(fiksClient, eventService)

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

        every { fiksClient.hentDigisosSak(any(), any()) } returns mockDigisosSak
        every { mockDigisosSak.ettersendtInfoNAV } returns mockk()
        every { mockDigisosSak.originalSoknadNAV?.timestampSendt } returns tidspunkt_sendt.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    @Test
    fun `Skal returnere respons med 1 hendelse`() {
        val model = InternalDigisosSoker()
        model.historikk.add(Hendelse(SOKNAD_SENDT, tittel_sendt, tidspunkt_sendt))

        every { eventService.createModel(any(), any()) } returns model

        val hendelser = service.hentHendelser("123", "Token")

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

        every { eventService.createModel(any(), any()) } returns model

        val hendelser = service.hentHendelser("123", "Token")

        assertThat(hendelser).hasSize(3)
    }
}