package no.nav.sbl.sosialhjelpmodiaapi.event

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.client.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsStatus
import no.nav.sbl.sosialhjelpmodiaapi.event.Titler.SOKNAD_FERDIGBEHANDLET
import no.nav.sbl.sosialhjelpmodiaapi.event.Titler.SOKNAD_MOTTATT
import no.nav.sbl.sosialhjelpmodiaapi.event.Titler.SOKNAD_SENDT
import no.nav.sbl.sosialhjelpmodiaapi.event.Titler.SOKNAD_UNDER_BEHANDLING
import no.nav.sbl.sosialhjelpmodiaapi.service.innsyn.InnsynService
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime
import no.nav.sbl.sosialhjelpmodiaapi.unixToLocalDateTime
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SoknadsStatusTest {

    private val innsynService: InnsynService = mockk()
    private val norgClient: NorgClient = mockk()

    private val service = EventService(innsynService, norgClient)

    private val mockDigisosSak: DigisosSak = mockk()

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
    fun `soknadsStatus SENDT`() {
        every { innsynService.hentJsonDigisosSoker(any(), any()) } returns null

        val model = service.createModel(mockDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.SENDT)
        assertThat(model.historikk).hasSize(1)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tidspunkt).isEqualTo(unixToLocalDateTime(tidspunkt_soknad))
        assertThat(hendelse.tittel).isEqualTo(SOKNAD_SENDT)
        assertThat(hendelse.beskrivelse).contains("Søknaden med vedlegg er sendt til")
    }

    @Test
    fun `soknadsStatus MOTTATT`() {
        every { innsynService.hentJsonDigisosSoker(any(), any()) } returns
                JsonDigisosSoker()
                        .withAvsender(avsender)
                        .withVersion("123")
                        .withHendelser(listOf(
                                SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1)
                        ))

        val model = service.createModel(mockDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.MOTTATT)
        assertThat(model.historikk).hasSize(2)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tidspunkt).isEqualTo(tidspunkt_1.toLocalDateTime())
        assertThat(hendelse.tittel).isEqualTo(SOKNAD_MOTTATT)
        assertThat(hendelse.beskrivelse).contains("Søknaden med vedlegg er mottatt hos ")
    }

    @Test
    fun `soknadsStatus MOTTATT papirsoknad`() {
        every { mockDigisosSak.tilleggsinformasjon?.enhetsnummer } returns null
        every { innsynService.hentJsonDigisosSoker(any(), any()) } returns
                JsonDigisosSoker()
                        .withAvsender(avsender)
                        .withVersion("123")
                        .withHendelser(listOf(
                                SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1)
                        ))

        val model = service.createModel(mockDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.MOTTATT)
        assertThat(model.historikk).hasSize(1)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tidspunkt).isEqualTo(tidspunkt_1.toLocalDateTime())
        assertThat(hendelse.tittel).isEqualTo(SOKNAD_MOTTATT)
        assertThat(hendelse.beskrivelse).contains("Søknaden med vedlegg er mottatt")
    }

    @Test
    fun `soknadsStatus UNDER_BEHANDLING`() {
        every { innsynService.hentJsonDigisosSoker(any(), any()) } returns
                JsonDigisosSoker()
                        .withAvsender(avsender)
                        .withVersion("123")
                        .withHendelser(listOf(
                                SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2)
                        ))

        val model = service.createModel(mockDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
        assertThat(model.saker).isEmpty()
        assertThat(model.historikk).hasSize(3)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tidspunkt).isEqualTo(tidspunkt_2.toLocalDateTime())
        assertThat(hendelse.tittel).isEqualTo(SOKNAD_UNDER_BEHANDLING)
        assertThat(hendelse.beskrivelse).isNull()
    }

    @Test
    fun `soknadsStatus FERDIGBEHANDLET`() {
        every { innsynService.hentJsonDigisosSoker(any(), any()) } returns
                JsonDigisosSoker()
                        .withAvsender(avsender)
                        .withVersion("123")
                        .withHendelser(listOf(
                                SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                                SOKNADS_STATUS_FERDIGBEHANDLET.withHendelsestidspunkt(tidspunkt_3)
                        ))

        val model = service.createModel(mockDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.FERDIGBEHANDLET)
        assertThat(model.saker).isEmpty()
        assertThat(model.historikk).hasSize(4)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tidspunkt).isEqualTo(tidspunkt_3.toLocalDateTime())
        assertThat(hendelse.tittel).isEqualTo(SOKNAD_FERDIGBEHANDLET)
        assertThat(hendelse.beskrivelse).isNull()
    }
}