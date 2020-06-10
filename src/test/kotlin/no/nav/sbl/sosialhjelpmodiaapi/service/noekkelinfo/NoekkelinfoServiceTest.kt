package no.nav.sbl.sosialhjelpmodiaapi.service.noekkelinfo

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.SOKNAD_DEFAULT_TITTEL
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.ForelopigSvar
import no.nav.sbl.sosialhjelpmodiaapi.domain.Hendelse
import no.nav.sbl.sosialhjelpmodiaapi.domain.InternalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.domain.NavKontorInformasjon
import no.nav.sbl.sosialhjelpmodiaapi.domain.SendingType
import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsStatus.MOTTATT
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.event.Titler.SOKNAD_SENDT
import no.nav.sbl.sosialhjelpmodiaapi.unixToLocalDateTime
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class NoekkelinfoServiceTest {

    private val fiksClient: FiksClient = mockk()
    private val eventService: EventService = mockk()
    private val service = NoekkelinfoService(fiksClient, eventService)

    private val mockDigisosSak: DigisosSak = mockk()

    private val enhetNavn1 = "NAV TestKontor"
    private val enhetsnr1 = "1234"

    private val enhetNavn2 = "NAV sekundært TestKontor"
    private val enhetsnr2 = "5678"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { fiksClient.hentDigisosSak(any(), any()) } returns mockDigisosSak
        every { mockDigisosSak.sistEndret } returns 123456789
    }

    @Test
    fun `noekkelinfo ikke videresendt eller forelopig svar`() {
        val tidspunkt = LocalDateTime.now()

        val model = InternalDigisosSoker()
        model.status = MOTTATT
        model.historikk.add(Hendelse(SOKNAD_SENDT, "søknad sendt", tidspunkt))
        model.navKontorHistorikk = mutableListOf(
                NavKontorInformasjon(SendingType.SENDT, LocalDateTime.now().minusDays(7), enhetsnr1, enhetNavn1)
        )

        every { eventService.createModel(any(), any()) } returns model
        every { mockDigisosSak.digisosSoker } returns null

        val noekkelinfo = service.hentNoekkelInfo("123", "token")

        assertThat(noekkelinfo.status).isEqualTo(MOTTATT)
        assertThat(noekkelinfo.tittel).isEqualTo(SOKNAD_DEFAULT_TITTEL)
        assertThat(noekkelinfo.sistOppdatert).isEqualTo(unixToLocalDateTime(123456789).toLocalDate())
        assertThat(noekkelinfo.saksId).isNull() //fix
        assertThat(noekkelinfo.sendtEllerMottattTidspunkt).isEqualTo(tidspunkt.toLocalDate())
        assertThat(noekkelinfo.navKontor?.enhetsNavn).isEqualTo(enhetNavn1)
        assertThat(noekkelinfo.navKontor?.enhetsNr).isEqualTo(enhetsnr1)
        assertThat(noekkelinfo.videresendtHistorikk).isNull()
        assertThat(noekkelinfo.tidspunktForelopigSvar).isNull()
    }

    @Test
    fun `noekkelinfo ikke videresendt med med forelopig svar`() {
        val tidspunkt = LocalDateTime.now()

        val model = InternalDigisosSoker()
        model.status = MOTTATT
        model.historikk.add(Hendelse(SOKNAD_SENDT, "søknad sendt", tidspunkt))
        model.navKontorHistorikk = mutableListOf(
                NavKontorInformasjon(SendingType.SENDT, LocalDateTime.now().minusDays(7), enhetsnr1, enhetNavn1)
        )
        model.forelopigSvar = ForelopigSvar(tidspunkt)

        every { eventService.createModel(any(), any()) } returns model
        every { mockDigisosSak.digisosSoker } returns null

        val noekkelinfo = service.hentNoekkelInfo("123", "token")

        assertThat(noekkelinfo.status).isEqualTo(MOTTATT)
        assertThat(noekkelinfo.tittel).isEqualTo(SOKNAD_DEFAULT_TITTEL)
        assertThat(noekkelinfo.sistOppdatert).isEqualTo(unixToLocalDateTime(123456789).toLocalDate())
        assertThat(noekkelinfo.saksId).isNull() //fix
        assertThat(noekkelinfo.sendtEllerMottattTidspunkt).isEqualTo(tidspunkt.toLocalDate())
        assertThat(noekkelinfo.navKontor?.enhetsNavn).isEqualTo(enhetNavn1)
        assertThat(noekkelinfo.navKontor?.enhetsNr).isEqualTo(enhetsnr1)
        assertThat(noekkelinfo.videresendtHistorikk).isNull()
        assertThat(noekkelinfo.tidspunktForelopigSvar).isEqualTo(tidspunkt)
    }

    @Test
    fun `noekkelinfo videresendt`() {
        val tidspunkt = LocalDateTime.now()

        val model = InternalDigisosSoker()
        model.status = MOTTATT
        model.historikk.add(Hendelse(SOKNAD_SENDT, "søknad sendt", tidspunkt))
        model.navKontorHistorikk = mutableListOf(
                NavKontorInformasjon(SendingType.SENDT, LocalDateTime.now().minusDays(7), enhetsnr1, enhetNavn1),
                NavKontorInformasjon(SendingType.VIDERESENDT, LocalDateTime.now().minusDays(4), enhetsnr2, enhetNavn2)
        )

        every { eventService.createModel(any(), any()) } returns model
        every { mockDigisosSak.digisosSoker } returns null

        val noekkelinfo = service.hentNoekkelInfo("123", "token")

        assertThat(noekkelinfo.status).isEqualTo(MOTTATT)
        assertThat(noekkelinfo.tittel).isEqualTo(SOKNAD_DEFAULT_TITTEL)
        assertThat(noekkelinfo.sistOppdatert).isEqualTo(unixToLocalDateTime(123456789).toLocalDate())
        assertThat(noekkelinfo.saksId).isNull() //fix
        assertThat(noekkelinfo.sendtEllerMottattTidspunkt).isEqualTo(tidspunkt.toLocalDate())
        assertThat(noekkelinfo.navKontor?.enhetsNavn).isEqualTo(enhetNavn2)
        assertThat(noekkelinfo.navKontor?.enhetsNr).isEqualTo(enhetsnr2)
        assertThat(noekkelinfo.videresendtHistorikk).hasSize(2)
        assertThat(noekkelinfo.tidspunktForelopigSvar).isNull()
    }

    @Test
    fun `noekkelinfo papirsoknad og videresendt`() {
        val tidspunkt = LocalDateTime.now()

        val model = InternalDigisosSoker()
        model.status = MOTTATT
        model.historikk.add(Hendelse(SOKNAD_SENDT, "søknad sendt", tidspunkt))
        model.navKontorHistorikk = mutableListOf(
                NavKontorInformasjon(SendingType.SENDT, LocalDateTime.now().minusDays(7), enhetsnr1, enhetNavn1),
                NavKontorInformasjon(SendingType.VIDERESENDT, LocalDateTime.now().minusDays(4), enhetsnr2, enhetNavn2)
        )

        every { eventService.createModel(any(), any()) } returns model
        every { mockDigisosSak.digisosSoker } returns null

        val noekkelinfo = service.hentNoekkelInfo("123", "token")

        assertThat(noekkelinfo.status).isEqualTo(MOTTATT)
        assertThat(noekkelinfo.tittel).isEqualTo(SOKNAD_DEFAULT_TITTEL)
        assertThat(noekkelinfo.sistOppdatert).isEqualTo(unixToLocalDateTime(123456789).toLocalDate())
        assertThat(noekkelinfo.saksId).isNull() //fix
        assertThat(noekkelinfo.sendtEllerMottattTidspunkt).isEqualTo(tidspunkt.toLocalDate())
        assertThat(noekkelinfo.navKontor?.enhetsNavn).isEqualTo(enhetNavn2)
        assertThat(noekkelinfo.navKontor?.enhetsNr).isEqualTo(enhetsnr2)
        assertThat(noekkelinfo.videresendtHistorikk).hasSize(2)
        assertThat(noekkelinfo.tidspunktForelopigSvar).isNull()
    }

    @Test
    fun `noekkelinfo papirsoknad ikke videresendt`() {
        val tidspunkt = LocalDateTime.now()

        val model = InternalDigisosSoker()
        model.status = MOTTATT
        model.historikk.add(Hendelse(SOKNAD_SENDT, "søknad sendt", tidspunkt))

        every { eventService.createModel(any(), any()) } returns model
        every { mockDigisosSak.digisosSoker } returns null

        val noekkelinfo = service.hentNoekkelInfo("123", "token")

        assertThat(noekkelinfo.status).isEqualTo(MOTTATT)
        assertThat(noekkelinfo.tittel).isEqualTo(SOKNAD_DEFAULT_TITTEL)
        assertThat(noekkelinfo.sistOppdatert).isEqualTo(unixToLocalDateTime(123456789).toLocalDate())
        assertThat(noekkelinfo.saksId).isNull() //fix
        assertThat(noekkelinfo.sendtEllerMottattTidspunkt).isEqualTo(tidspunkt.toLocalDate())
        assertThat(noekkelinfo.navKontor).isNull()
        assertThat(noekkelinfo.videresendtHistorikk).isNull()
        assertThat(noekkelinfo.tidspunktForelopigSvar).isNull()
    }
}