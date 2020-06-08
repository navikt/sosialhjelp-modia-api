package no.nav.sbl.sosialhjelpmodiaapi.event

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.client.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.common.NorgException
import no.nav.sbl.sosialhjelpmodiaapi.domain.DigisosSak
import no.nav.sbl.sosialhjelpmodiaapi.domain.SendingType
import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsStatus
import no.nav.sbl.sosialhjelpmodiaapi.event.Titler.SOKNAD_MOTTATT
import no.nav.sbl.sosialhjelpmodiaapi.event.Titler.SOKNAD_VIDERESENDT
import no.nav.sbl.sosialhjelpmodiaapi.service.innsyn.InnsynService
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

internal class TildeltNavKontorTest {

    private val innsynService: InnsynService = mockk()
    private val norgClient: NorgClient = mockk()
    private val service = EventService(innsynService, norgClient)

    private val mockDigisosSak: DigisosSak = mockk()

    private val enhetNavn = "NAV Holmenkollen"
    private val enhetNavn2 = "NAV Longyearbyen"

    @BeforeEach
    fun init() {
        clearAllMocks()
        every { mockDigisosSak.fiksDigisosId } returns "123"
        every { mockDigisosSak.digisosSoker?.metadata } returns "some id"
        every { mockDigisosSak.originalSoknadNAV?.metadata } returns "some other id"
        every { mockDigisosSak.originalSoknadNAV?.timestampSendt } returns tidspunkt_soknad
        every { mockDigisosSak.tilleggsinformasjon?.enhetsnummer } returns enhetsnr
        every { norgClient.hentNavEnhet(enhetsnr).navn } returns enhetsnavn

        resetHendelser()
    }

    @Test
    fun `tildeltNavKontor skal hente navenhets navn fra Norg`() {
        every { norgClient.hentNavEnhet(navKontor).navn } returns enhetNavn
        every { innsynService.hentJsonDigisosSoker(any(), any()) } returns
                JsonDigisosSoker()
                        .withAvsender(avsender)
                        .withVersion("123")
                        .withHendelser(listOf(
                                SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                TILDELT_NAV_KONTOR.withHendelsestidspunkt(tidspunkt_2)))

        val model = service.createModel(mockDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.MOTTATT)
        assertThat(model.saker).hasSize(0)
        assertThat(model.historikk).hasSize(3)
        assertThat(model.navKontorHistorikk).hasSize(2)

        val last = model.navKontorHistorikk.last()
        assertThat(last.type).isEqualTo(SendingType.VIDERESENDT)
        assertThat(last.tidspunkt).isEqualTo(tidspunkt_2.toLocalDateTime())
        assertThat(last.navEnhetsnummer).isEqualTo(navKontor)
        assertThat(last.navEnhetsnavn).isEqualTo(enhetNavn)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tittel).isEqualTo(SOKNAD_VIDERESENDT)
        assertThat(hendelse.beskrivelse).contains(enhetNavn)
    }

    @Test
    fun `tildeltNavKontor skal gi generell melding hvis NorgClient kaster FiksException`() {
        every { norgClient.hentNavEnhet(navKontor) } throws NorgException(HttpStatus.INTERNAL_SERVER_ERROR, "noe feilet", null)
        every { innsynService.hentJsonDigisosSoker(any(), any()) } returns
                JsonDigisosSoker()
                        .withAvsender(avsender)
                        .withVersion("123")
                        .withHendelser(listOf(
                                SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                TILDELT_NAV_KONTOR.withHendelsestidspunkt(tidspunkt_2)))

        val model = service.createModel(mockDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.MOTTATT)
        assertThat(model.saker).hasSize(0)
        assertThat(model.historikk).hasSize(3)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tittel).isEqualTo(SOKNAD_VIDERESENDT)
        assertThat(hendelse.beskrivelse)
                .doesNotContain(enhetNavn)
                .contains("et annet NAV-kontor")
    }

    @Test
    fun `tildeltNavKontor til samme navKontor som soknad ble sendt til - gir ingen hendelse`() {
        every { mockDigisosSak.tilleggsinformasjon?.enhetsnummer } returns navKontor
        every { norgClient.hentNavEnhet(navKontor).navn } returns enhetNavn
        every { innsynService.hentJsonDigisosSoker(any(), any()) } returns
                JsonDigisosSoker()
                        .withAvsender(avsender)
                        .withVersion("123")
                        .withHendelser(listOf(
                                SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                TILDELT_NAV_KONTOR.withHendelsestidspunkt(tidspunkt_2)))

        val model = service.createModel(mockDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.MOTTATT)
        assertThat(model.saker).hasSize(0)
        assertThat(model.historikk).hasSize(2)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tittel).isEqualTo(SOKNAD_MOTTATT)
    }

    @Test
    fun `flere identiske tildeltNavKontor-hendelser skal kun gi en hendelse i historikk`() {
        every { norgClient.hentNavEnhet(navKontor).navn } returns enhetNavn
        every { innsynService.hentJsonDigisosSoker(any(), any()) } returns
                JsonDigisosSoker()
                        .withAvsender(avsender)
                        .withVersion("123")
                        .withHendelser(listOf(
                                SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                TILDELT_NAV_KONTOR.withHendelsestidspunkt(tidspunkt_2),
                                TILDELT_NAV_KONTOR.withHendelsestidspunkt(tidspunkt_3)))

        val model = service.createModel(mockDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.MOTTATT)
        assertThat(model.saker).hasSize(0)
        assertThat(model.historikk).hasSize(3)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tittel).isEqualTo(SOKNAD_VIDERESENDT)
        assertThat(hendelse.beskrivelse).contains(enhetNavn)
    }

    @Test
    fun `tildeltNavKontor til ulike kontor gir like mange hendelser`() {
        every { norgClient.hentNavEnhet(navKontor).navn } returns enhetNavn
        every { norgClient.hentNavEnhet(navKontor2).navn } returns enhetNavn2
        every { innsynService.hentJsonDigisosSoker(any(), any()) } returns
                JsonDigisosSoker()
                        .withAvsender(avsender)
                        .withVersion("123")
                        .withHendelser(listOf(
                                SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                TILDELT_NAV_KONTOR.withHendelsestidspunkt(tidspunkt_2),
                                TILDELT_NAV_KONTOR_2.withHendelsestidspunkt(tidspunkt_3)))

        val model = service.createModel(mockDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.MOTTATT)
        assertThat(model.saker).hasSize(0)
        assertThat(model.historikk).hasSize(4)

        assertThat(model.historikk[2].tittel).isEqualTo(SOKNAD_VIDERESENDT)
        assertThat(model.historikk[2].beskrivelse).contains(enhetNavn)

        assertThat(model.historikk[3].tittel).isEqualTo(SOKNAD_VIDERESENDT)
        assertThat(model.historikk[3].beskrivelse).contains(enhetNavn2)
    }
}