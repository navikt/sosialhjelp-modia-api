package no.nav.sosialhjelp.modia.soknad.noekkelinfo

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.modia.client.fiks.FiksClient
import no.nav.sosialhjelp.modia.domain.ForelopigSvar
import no.nav.sosialhjelp.modia.domain.Hendelse
import no.nav.sosialhjelp.modia.domain.InternalDigisosSoker
import no.nav.sosialhjelp.modia.domain.NavKontorInformasjon
import no.nav.sosialhjelp.modia.domain.SendingType
import no.nav.sosialhjelp.modia.domain.SoknadsStatus.MOTTATT
import no.nav.sosialhjelp.modia.event.EventService
import no.nav.sosialhjelp.modia.event.SOKNAD_DEFAULT_TITTEL
import no.nav.sosialhjelp.modia.event.Titler.SOKNAD_SENDT
import no.nav.sosialhjelp.modia.kommune.KommuneService
import no.nav.sosialhjelp.modia.kommune.KommunenavnService
import no.nav.sosialhjelp.modia.unixToLocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class NoekkelinfoServiceTest {

    private val fiksClient: FiksClient = mockk()
    private val eventService: EventService = mockk()
    private val kommunenavnService: KommunenavnService = mockk()
    private val kommuneService: KommuneService = mockk()
    private val service = NoekkelinfoService(fiksClient, eventService, kommunenavnService, kommuneService)

    private val mockDigisosSak: DigisosSak = mockk()

    private val enhetNavn1 = "NAV TestKontor"
    private val enhetsnr1 = "1234"
    private val kommunenr = "2468"
    private val kommunenavn = "Oslo"

    private val enhetNavn2 = "NAV sekundært TestKontor"
    private val enhetsnr2 = "5678"
    private val kommunenavn2 = "Nabo"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { fiksClient.hentDigisosSak(any()) } returns mockDigisosSak
        every { mockDigisosSak.sistEndret } returns 123456789
        every { mockDigisosSak.kommunenummer } returns kommunenr
        every { kommunenavnService.hentKommunenavnFor(any()) } returns kommunenavn
        every { kommuneService.getBehandlingsanvarligKommune(any()) } returns kommunenavn
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

        every { eventService.createModel(any()) } returns model
        every { mockDigisosSak.digisosSoker } returns null

        val noekkelinfo = service.hentNoekkelInfo("123")

        assertThat(noekkelinfo.status).isEqualTo(MOTTATT)
        assertThat(noekkelinfo.tittel).isEqualTo(SOKNAD_DEFAULT_TITTEL)
        assertThat(noekkelinfo.sistOppdatert).isEqualTo(unixToLocalDateTime(123456789).toLocalDate())
        assertThat(noekkelinfo.sendtEllerMottattTidspunkt).isEqualTo(tidspunkt.toLocalDate())
        assertThat(noekkelinfo.navKontor?.enhetsNavn).isEqualTo(enhetNavn1)
        assertThat(noekkelinfo.navKontor?.enhetsNr).isEqualTo(enhetsnr1)
        assertThat(noekkelinfo.kommunenavn).isEqualTo(kommunenavn)
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

        every { eventService.createModel(any()) } returns model
        every { mockDigisosSak.digisosSoker } returns null

        val noekkelinfo = service.hentNoekkelInfo("123")

        assertThat(noekkelinfo.status).isEqualTo(MOTTATT)
        assertThat(noekkelinfo.tittel).isEqualTo(SOKNAD_DEFAULT_TITTEL)
        assertThat(noekkelinfo.sistOppdatert).isEqualTo(unixToLocalDateTime(123456789).toLocalDate())
        assertThat(noekkelinfo.sendtEllerMottattTidspunkt).isEqualTo(tidspunkt.toLocalDate())
        assertThat(noekkelinfo.navKontor?.enhetsNavn).isEqualTo(enhetNavn1)
        assertThat(noekkelinfo.navKontor?.enhetsNr).isEqualTo(enhetsnr1)
        assertThat(noekkelinfo.kommunenavn).isEqualTo(kommunenavn)
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

        every { eventService.createModel(any()) } returns model
        every { mockDigisosSak.digisosSoker } returns null

        val noekkelinfo = service.hentNoekkelInfo("123")

        assertThat(noekkelinfo.status).isEqualTo(MOTTATT)
        assertThat(noekkelinfo.tittel).isEqualTo(SOKNAD_DEFAULT_TITTEL)
        assertThat(noekkelinfo.sistOppdatert).isEqualTo(unixToLocalDateTime(123456789).toLocalDate())
        assertThat(noekkelinfo.sendtEllerMottattTidspunkt).isEqualTo(tidspunkt.toLocalDate())
        assertThat(noekkelinfo.navKontor?.enhetsNavn).isEqualTo(enhetNavn2)
        assertThat(noekkelinfo.navKontor?.enhetsNr).isEqualTo(enhetsnr2)
        assertThat(noekkelinfo.kommunenavn).isEqualTo(kommunenavn)
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

        every { eventService.createModel(any()) } returns model
        every { mockDigisosSak.digisosSoker } returns null

        val noekkelinfo = service.hentNoekkelInfo("123")

        assertThat(noekkelinfo.status).isEqualTo(MOTTATT)
        assertThat(noekkelinfo.tittel).isEqualTo(SOKNAD_DEFAULT_TITTEL)
        assertThat(noekkelinfo.sistOppdatert).isEqualTo(unixToLocalDateTime(123456789).toLocalDate())
        assertThat(noekkelinfo.sendtEllerMottattTidspunkt).isEqualTo(tidspunkt.toLocalDate())
        assertThat(noekkelinfo.navKontor?.enhetsNavn).isEqualTo(enhetNavn2)
        assertThat(noekkelinfo.navKontor?.enhetsNr).isEqualTo(enhetsnr2)
        assertThat(noekkelinfo.kommunenavn).isEqualTo(kommunenavn)
        assertThat(noekkelinfo.videresendtHistorikk).hasSize(2)
        assertThat(noekkelinfo.tidspunktForelopigSvar).isNull()
    }

    @Test
    fun `noekkelinfo papirsoknad ikke videresendt`() {
        val tidspunkt = LocalDateTime.now()

        val model = InternalDigisosSoker()
        model.status = MOTTATT
        model.historikk.add(Hendelse(SOKNAD_SENDT, "søknad sendt", tidspunkt))

        every { eventService.createModel(any()) } returns model
        every { mockDigisosSak.digisosSoker } returns null

        val noekkelinfo = service.hentNoekkelInfo("123")

        assertThat(noekkelinfo.status).isEqualTo(MOTTATT)
        assertThat(noekkelinfo.tittel).isEqualTo(SOKNAD_DEFAULT_TITTEL)
        assertThat(noekkelinfo.sistOppdatert).isEqualTo(unixToLocalDateTime(123456789).toLocalDate())
        assertThat(noekkelinfo.sendtEllerMottattTidspunkt).isEqualTo(tidspunkt.toLocalDate())
        assertThat(noekkelinfo.navKontor).isNull()
        assertThat(noekkelinfo.kommunenavn).isEqualTo(kommunenavn)
        assertThat(noekkelinfo.videresendtHistorikk).isNull()
        assertThat(noekkelinfo.tidspunktForelopigSvar).isNull()
    }

    @Test
    fun `behandlende kommune returneres som kommunenavn hvis satt`() {
        val model = InternalDigisosSoker()
        model.status = MOTTATT
        model.historikk.add(Hendelse(SOKNAD_SENDT, "søknad sendt", LocalDateTime.now()))

        every { eventService.createModel(any()) } returns model
        every { mockDigisosSak.digisosSoker } returns null
        every { kommuneService.getBehandlingsanvarligKommune(any()) } returns kommunenavn2

        val noekkelinfo = service.hentNoekkelInfo("123")

        assertThat(noekkelinfo.kommunenavn).isEqualTo(kommunenavn2)
    }

    @Test
    fun `kommunenavn returneres som kommunenavn hvis behandlingsansvarlig ikke satt`() {
        val model = InternalDigisosSoker()
        model.status = MOTTATT
        model.historikk.add(Hendelse(SOKNAD_SENDT, "søknad sendt", LocalDateTime.now()))

        every { eventService.createModel(any()) } returns model
        every { mockDigisosSak.digisosSoker } returns null
        every { kommuneService.getBehandlingsanvarligKommune(any()) } returns null

        val noekkelinfo = service.hentNoekkelInfo("123")

        assertThat(noekkelinfo.kommunenavn).isEqualTo(kommunenavn)
    }
}
