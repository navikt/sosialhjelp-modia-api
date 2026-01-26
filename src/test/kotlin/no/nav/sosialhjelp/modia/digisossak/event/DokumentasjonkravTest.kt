package no.nav.sosialhjelp.modia.digisossak.event

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.OppgaveStatus
import no.nav.sosialhjelp.modia.digisossak.domain.SoknadsStatus
import no.nav.sosialhjelp.modia.navkontor.norg.NorgClient
import no.nav.sosialhjelp.modia.soknad.vedlegg.SoknadVedleggService
import no.nav.sosialhjelp.modia.soknad.vedlegg.VEDLEGG_KREVES_STATUS
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DokumentasjonkravTest {
    private val jsonDigisosSokerService: JsonDigisosSokerService = mockk()
    private val norgClient: NorgClient = mockk()
    private val soknadVedleggService: SoknadVedleggService = mockk()

    private val service = EventService(jsonDigisosSokerService, norgClient, soknadVedleggService)

    @BeforeEach
    fun init() {
        clearAllMocks()

        every { norgClient.hentNavEnhet(ENHETSNR)!!.navn } returns ENHETSNAVN

        every { soknadVedleggService.hentSoknadVedleggMedStatus(any(), VEDLEGG_KREVES_STATUS) } returns emptyList()

        resetHendelser()
    }

    @Test
    fun `dokumentasjonskrav ETTER utbetaling`() {
        every { jsonDigisosSokerService.get(any(), any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                        SAK1_VEDTAK_FATTET_INNVILGET.withHendelsestidspunkt(tidspunkt_3),
                        SOKNADS_STATUS_FERDIGBEHANDLET.withHendelsestidspunkt(tidspunkt_4),
                        UTBETALING.withHendelsestidspunkt(tidspunkt_5),
                        DOKUMENTASJONKRAV_RELEVANT.withHendelsestidspunkt(tidspunkt_6),
                    ),
                )

        val model = service.createModel(defaultDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.FERDIGBEHANDLET)
        assertThat(model.saker).hasSize(1)
        assertThat(model.historikk).hasSize(5)

        assertThat(model.saker[0].utbetalinger).hasSize(1)
        val utbetaling = model.saker[0].utbetalinger[0]
        assertThat(utbetaling.dokumentasjonkrav).hasSize(1)
        assertThat(utbetaling.dokumentasjonkrav[0].dokumentasjonkravId).isEqualTo(DOKUMENTASJONSKRAV)
        assertThat(utbetaling.dokumentasjonkrav[0].beskrivelse).isEqualTo("beskrivelse")
        assertThat(utbetaling.dokumentasjonkrav[0].status).isEqualTo(OppgaveStatus.RELEVANT)
    }

    @Test
    fun `dokumentasjonkrav UTEN utbetaling`() {
        every { jsonDigisosSokerService.get(any(), any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                        DOKUMENTASJONKRAV_OPPFYLT.withHendelsestidspunkt(tidspunkt_3),
                    ),
                )

        val model = service.createModel(defaultDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
        assertThat(model.saker).hasSize(0)
        assertThat(model.historikk).hasSize(3)
    }

    @Test
    fun `dokumentasjonkrav FOR utbetaling - skal ikke gi noen dokumentasjonkrav`() {
        every { jsonDigisosSokerService.get(any(), any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                        SAK1_SAKS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_3),
                        SOKNADS_STATUS_FERDIGBEHANDLET.withHendelsestidspunkt(tidspunkt_4),
                        DOKUMENTASJONKRAV_OPPFYLT.withHendelsestidspunkt(tidspunkt_5),
                        UTBETALING.withHendelsestidspunkt(tidspunkt_6),
                    ),
                )

        val model = service.createModel(defaultDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.saker).hasSize(1)

        assertThat(model.saker[0].utbetalinger).hasSize(1)
        val utbetaling = model.saker[0].utbetalinger[0]
        assertThat(utbetaling.dokumentasjonkrav).hasSize(0)
    }

    @Test
    fun `dokumentasjonkrav og utbetaling har identiske hendelsestidspunkt`() {
        every { jsonDigisosSokerService.get(any(), any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                        SAK1_SAKS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_3),
                        SOKNADS_STATUS_FERDIGBEHANDLET.withHendelsestidspunkt(tidspunkt_4),
                        DOKUMENTASJONKRAV_OPPFYLT.withHendelsestidspunkt(tidspunkt_5),
                        UTBETALING.withHendelsestidspunkt(tidspunkt_5),
                    ),
                )

        val model = service.createModel(defaultDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.saker).hasSize(1)

        assertThat(model.saker[0].utbetalinger).hasSize(1)
        val utbetaling = model.saker[0].utbetalinger[0]
        assertThat(utbetaling.dokumentasjonkrav).hasSize(1)
        assertThat(utbetaling.dokumentasjonkrav[0].dokumentasjonkravId).isEqualTo(DOKUMENTASJONSKRAV)
    }
}
