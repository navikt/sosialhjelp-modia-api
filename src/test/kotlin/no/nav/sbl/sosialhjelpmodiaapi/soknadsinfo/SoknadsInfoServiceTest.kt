package no.nav.sbl.sosialhjelpmodiaapi.soknadsinfo

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.SOKNAD_DEFAULT_TITTEL
import no.nav.sbl.sosialhjelpmodiaapi.domain.*
import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsStatus.MOTTATT
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.unixToLocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class SoknadsInfoServiceTest {

    private val fiksClient: FiksClient = mockk()
    private val eventService: EventService = mockk()
    private val norgClient: NorgClient = mockk()
    private val service = SoknadsInfoService(fiksClient, eventService, norgClient)

    private val mockDigisosSak: DigisosSak = mockk()
    private val mockNavEnhetSendt: NavEnhet = mockk()
    private val mockNavEnhetVideresendt: NavEnhet = mockk()

    private val enhetNavn1 = "NAV TestKontor"
    private val enhetsnr1 = "1234"
    private val sosialetjenester1 = "numero uno"

    private val enhetNavn2 = "NAV sekundært TestKontor"
    private val enhetsnr2 = "5678"
    private val sosialetjenester2 = "numero dos"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { fiksClient.hentDigisosSak(any(), any()) } returns mockDigisosSak

        every { mockDigisosSak.sistEndret } returns 123456789

        every { mockNavEnhetSendt.navn } returns enhetNavn1
        every { mockNavEnhetSendt.sosialeTjenester } returns sosialetjenester1

        every { mockNavEnhetVideresendt.navn } returns enhetNavn2
        every { mockNavEnhetVideresendt.sosialeTjenester } returns sosialetjenester2

        every { norgClient.hentNavEnhet(enhetsnr1) } returns mockNavEnhetSendt
        every { norgClient.hentNavEnhet(enhetsnr2) } returns mockNavEnhetVideresendt
    }

    @Test
    fun `soknadsInfo ikke videresendt eller forelopig svar`() {
        val tidspunkt = LocalDateTime.now()

        val model = InternalDigisosSoker()
        model.status = MOTTATT
        model.historikk.add(Hendelse("søknad sendt", tidspunkt))
        model.soknadsmottaker = Soknadsmottaker(enhetsnr1, enhetNavn1)

        every { eventService.createModel(any(), any()) } returns model

        every { mockDigisosSak.digisosSoker } returns null

        val soknadsInfo = service.hentSoknadsInfo("123", "token")

        assertThat(soknadsInfo.status).isEqualTo(MOTTATT)
        assertThat(soknadsInfo.tittel).isEqualTo(SOKNAD_DEFAULT_TITTEL)
        assertThat(soknadsInfo.sistOppdatert).isEqualTo(unixToLocalDateTime(123456789))
        assertThat(soknadsInfo.sendtTidspunkt).isEqualTo(tidspunkt)
        assertThat(soknadsInfo.navKontorSoknad).isEqualTo(enhetNavn1)
        assertThat(soknadsInfo.navKontorTildelt).isNull()
        assertThat(soknadsInfo.tidspunktForelopigSvar).isNull()
        assertThat(soknadsInfo.navKontorSaksbehandlingstid).isEqualTo(sosialetjenester1)
    }

    @Test
    fun `soknadsInfo ikke videresendt med med forelopig svar`() {
        val tidspunkt = LocalDateTime.now()

        val model = InternalDigisosSoker()
        model.status = MOTTATT
        model.historikk.add(Hendelse("søknad sendt", tidspunkt))
        model.soknadsmottaker = Soknadsmottaker(enhetsnr1, enhetNavn1)
        model.forelopigSvar = ForelopigSvar(tidspunkt)

        every { eventService.createModel(any(), any()) } returns model

        every { mockDigisosSak.digisosSoker } returns null

        val soknadsInfo = service.hentSoknadsInfo("123", "token")

        assertThat(soknadsInfo.status).isEqualTo(MOTTATT)
        assertThat(soknadsInfo.tittel).isEqualTo(SOKNAD_DEFAULT_TITTEL)
        assertThat(soknadsInfo.sistOppdatert).isEqualTo(unixToLocalDateTime(123456789))
        assertThat(soknadsInfo.sendtTidspunkt).isEqualTo(tidspunkt)
        assertThat(soknadsInfo.navKontorSoknad).isEqualTo(enhetNavn1)
        assertThat(soknadsInfo.navKontorTildelt).isNull()
        assertThat(soknadsInfo.tidspunktForelopigSvar).isEqualTo(tidspunkt)
        assertThat(soknadsInfo.navKontorSaksbehandlingstid).isEqualTo(sosialetjenester1)
    }

    @Test
    fun `soknadsInfo videresendt`() {
        val tidspunkt = LocalDateTime.now()

        val model = InternalDigisosSoker()
        model.status = MOTTATT
        model.historikk.add(Hendelse("søknad sendt", tidspunkt))
        model.soknadsmottaker = Soknadsmottaker(enhetsnr1, enhetNavn1)
        model.tildeltNavKontor = enhetsnr2

        every { eventService.createModel(any(), any()) } returns model

        every { mockDigisosSak.digisosSoker } returns null

        val soknadsInfo = service.hentSoknadsInfo("123", "token")

        assertThat(soknadsInfo.status).isEqualTo(MOTTATT)
        assertThat(soknadsInfo.tittel).isEqualTo(SOKNAD_DEFAULT_TITTEL)
        assertThat(soknadsInfo.sistOppdatert).isEqualTo(unixToLocalDateTime(123456789))
        assertThat(soknadsInfo.sendtTidspunkt).isEqualTo(tidspunkt)
        assertThat(soknadsInfo.navKontorSoknad).isEqualTo(enhetNavn1)
        assertThat(soknadsInfo.navKontorTildelt).isEqualTo(enhetNavn2)
        assertThat(soknadsInfo.tidspunktForelopigSvar).isNull()
        assertThat(soknadsInfo.navKontorSaksbehandlingstid).isEqualTo(sosialetjenester2)
    }

    @Test
    fun `soknadsInfo papirsoknad og videresendt`() {
        val tidspunkt = LocalDateTime.now()

        val model = InternalDigisosSoker()
        model.status = MOTTATT
        model.historikk.add(Hendelse("søknad sendt", tidspunkt))
        model.tildeltNavKontor = enhetsnr2

        every { eventService.createModel(any(), any()) } returns model

        every { mockDigisosSak.digisosSoker } returns null

        val soknadsInfo = service.hentSoknadsInfo("123", "token")

        assertThat(soknadsInfo.status).isEqualTo(MOTTATT)
        assertThat(soknadsInfo.tittel).isEqualTo(SOKNAD_DEFAULT_TITTEL)
        assertThat(soknadsInfo.sistOppdatert).isEqualTo(unixToLocalDateTime(123456789))
        assertThat(soknadsInfo.sendtTidspunkt).isEqualTo(tidspunkt)
        assertThat(soknadsInfo.navKontorSoknad).isNull() // null hvis papirsøknad?
        assertThat(soknadsInfo.navKontorTildelt).isEqualTo(enhetNavn2)
        assertThat(soknadsInfo.tidspunktForelopigSvar).isNull()
        assertThat(soknadsInfo.navKontorSaksbehandlingstid).isEqualTo(sosialetjenester2)
    }

    @Test
    fun `soknadsInfo papirsoknad ikke videresendt`() {
        val tidspunkt = LocalDateTime.now()

        val model = InternalDigisosSoker()
        model.status = MOTTATT
        model.historikk.add(Hendelse("søknad sendt", tidspunkt))

        every { eventService.createModel(any(), any()) } returns model

        every { mockDigisosSak.digisosSoker } returns null

        val soknadsInfo = service.hentSoknadsInfo("123", "token")

        assertThat(soknadsInfo.status).isEqualTo(MOTTATT)
        assertThat(soknadsInfo.tittel).isEqualTo(SOKNAD_DEFAULT_TITTEL)
        assertThat(soknadsInfo.sistOppdatert).isEqualTo(unixToLocalDateTime(123456789))
        assertThat(soknadsInfo.sendtTidspunkt).isEqualTo(tidspunkt)
        assertThat(soknadsInfo.navKontorSoknad).isNull() // null hvis papirsøknad?
        assertThat(soknadsInfo.navKontorTildelt).isNull()
        assertThat(soknadsInfo.tidspunktForelopigSvar).isNull()
        assertThat(soknadsInfo.navKontorSaksbehandlingstid).isNull()
    }
}