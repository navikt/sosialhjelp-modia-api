package no.nav.sosialhjelp.modia.event

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.modia.app.exceptions.NorgException
import no.nav.sosialhjelp.modia.domain.SendingType
import no.nav.sosialhjelp.modia.domain.SoknadsStatus
import no.nav.sosialhjelp.modia.event.Titler.SOKNAD_MOTTATT
import no.nav.sosialhjelp.modia.event.Titler.SOKNAD_VIDERESENDT
import no.nav.sosialhjelp.modia.navkontor.norg.NorgClient
import no.nav.sosialhjelp.modia.soknad.vedlegg.SoknadVedleggService
import no.nav.sosialhjelp.modia.soknad.vedlegg.VEDLEGG_KREVES_STATUS
import no.nav.sosialhjelp.modia.toLocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TildeltNavKontorTest {

    private val jsonDigisosSokerService: JsonDigisosSokerService = mockk()
    private val norgClient: NorgClient = mockk()
    private val soknadVedleggService: SoknadVedleggService = mockk()

    private val service = EventService(jsonDigisosSokerService, norgClient, soknadVedleggService)

    private val mockDigisosSak: DigisosSak = mockk()

    private val enhetNavn = "NAV Holmenkollen"
    private val enhetNavn2 = "NAV Longyearbyen"

    @BeforeEach
    fun init() {
        clearAllMocks()
        every { mockDigisosSak.fiksDigisosId } returns "123"
        every { mockDigisosSak.sokerFnr } returns "fnr"
        every { mockDigisosSak.digisosSoker?.metadata } returns "some id"
        every { mockDigisosSak.originalSoknadNAV?.metadata } returns "some other id"
        every { mockDigisosSak.originalSoknadNAV?.timestampSendt } returns tidspunkt_soknad
        every { mockDigisosSak.tilleggsinformasjon?.enhetsnummer } returns enhetsnr
        every { norgClient.hentNavEnhet(enhetsnr)!!.navn } returns enhetsnavn

        every { soknadVedleggService.hentSoknadVedleggMedStatus(any(), VEDLEGG_KREVES_STATUS) } returns emptyList()

        resetHendelser()
    }

    @Test
    fun `tildeltNavKontor skal hente navenhets navn fra Norg`() {
        every { norgClient.hentNavEnhet(navKontor)!!.navn } returns enhetNavn
        every { jsonDigisosSokerService.get(any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        TILDELT_NAV_KONTOR.withHendelsestidspunkt(tidspunkt_2)
                    )
                )

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
    fun `tildeltNavKontor med tom navenhetsnummer skal gi default navenhetsnavn`() {
        every { norgClient.hentNavEnhet("") } returns null
        every { jsonDigisosSokerService.get(any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        TILDELT_EMPTY_NAV_KONTOR.withHendelsestidspunkt(tidspunkt_2)
                    )
                )

        val model = service.createModel(mockDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.MOTTATT)
        assertThat(model.saker).hasSize(0)
        assertThat(model.historikk).hasSize(3)
        assertThat(model.navKontorHistorikk).hasSize(2)

        val last = model.navKontorHistorikk.last()
        assertThat(last.type).isEqualTo(SendingType.VIDERESENDT)
        assertThat(last.tidspunkt).isEqualTo(tidspunkt_2.toLocalDateTime())
        assertThat(last.navEnhetsnummer).isEqualTo("")
        assertThat(last.navEnhetsnavn).isEqualTo(DEFAULT_NAVENHETSNAVN)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tittel).isEqualTo(SOKNAD_VIDERESENDT)
        assertThat(hendelse.beskrivelse).contains(DEFAULT_NAVENHETSNAVN)
    }

    @Test
    fun `tildeltNavKontor skal gi generell melding hvis NorgClient kaster FiksException`() {
        every { norgClient.hentNavEnhet(navKontor) } throws NorgException("noe feilet", null)
        every { jsonDigisosSokerService.get(any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        TILDELT_NAV_KONTOR.withHendelsestidspunkt(tidspunkt_2)
                    )
                )

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
        every { norgClient.hentNavEnhet(navKontor)!!.navn } returns enhetNavn
        every { jsonDigisosSokerService.get(any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        TILDELT_NAV_KONTOR.withHendelsestidspunkt(tidspunkt_2)
                    )
                )

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
        every { norgClient.hentNavEnhet(navKontor)!!.navn } returns enhetNavn
        every { jsonDigisosSokerService.get(any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        TILDELT_NAV_KONTOR.withHendelsestidspunkt(tidspunkt_2),
                        TILDELT_NAV_KONTOR.withHendelsestidspunkt(tidspunkt_3)
                    )
                )

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
        every { norgClient.hentNavEnhet(navKontor)!!.navn } returns enhetNavn
        every { norgClient.hentNavEnhet(navKontor2)!!.navn } returns enhetNavn2
        every { jsonDigisosSokerService.get(any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        TILDELT_NAV_KONTOR.withHendelsestidspunkt(tidspunkt_2),
                        TILDELT_NAV_KONTOR_2.withHendelsestidspunkt(tidspunkt_3)
                    )
                )

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
