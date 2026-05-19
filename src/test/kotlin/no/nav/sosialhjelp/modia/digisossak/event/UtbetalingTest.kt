package no.nav.sosialhjelp.modia.digisossak.event

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.SoknadsStatus
import no.nav.sosialhjelp.modia.digisossak.domain.UtbetalingsStatus
import no.nav.sosialhjelp.modia.navkontor.norg.NorgClient
import no.nav.sosialhjelp.modia.soknad.vedlegg.SoknadVedleggService
import no.nav.sosialhjelp.modia.soknad.vedlegg.VEDLEGG_KREVES_STATUS
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class UtbetalingTest {
    private val jsonDigisosSokerService: JsonDigisosSokerService = mockk()
    private val norgClient: NorgClient = mockk()
    private val soknadVedleggService: SoknadVedleggService = mockk()

    private val service = EventService(jsonDigisosSokerService, norgClient, soknadVedleggService)

    @BeforeEach
    fun init() {
        clearAllMocks()

        every { norgClient.hentNavEnhet(ENHETSNR)!!.navn } returns ENHETSNAVN

        coEvery { soknadVedleggService.hentSoknadVedleggMedStatus(any(), VEDLEGG_KREVES_STATUS) } returns emptyList()

        resetHendelser()
    }

    @Test
    suspend fun `utbetaling ETTER vedtakFattet og saksStatus`() {
        coEvery { jsonDigisosSokerService.get(any(), any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                        SAK1_SAKS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_3),
                        SAK1_VEDTAK_FATTET_INNVILGET.withHendelsestidspunkt(tidspunkt_4),
                        SOKNADS_STATUS_FERDIGBEHANDLET.withHendelsestidspunkt(tidspunkt_5),
                        UTBETALING.withHendelsestidspunkt(tidspunkt_6),
                    ),
                )

        val model = service.createModel(defaultDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.FERDIGBEHANDLET)
        assertThat(model.saker).hasSize(1)
        assertThat(model.historikk).hasSize(6)

        assertThat(model.saker[0].tittel).isEqualTo(TITTEL_1) // tittel for sak fra saksstatus-hendelse

        assertThat(model.saker[0].utbetalinger).hasSize(1)
        val utbetaling = model.saker[0].utbetalinger[0]
        assertThat(utbetaling.referanse).isEqualTo(UTBETALING_REF_1)
        assertThat(utbetaling.status).isEqualTo(UtbetalingsStatus.UTBETALT)
        assertThat(utbetaling.belop).isEqualTo("1234.56")
        assertThat(utbetaling.beskrivelse).isEqualTo(TITTEL_1)
        assertThat(utbetaling.forfallsDato).isEqualTo("2019-12-31")
        assertThat(utbetaling.utbetalingsDato).isEqualTo("2019-12-24")
        assertThat(utbetaling.fom).isEqualTo("2019-12-01")
        assertThat(utbetaling.tom).isEqualTo("2019-12-31")
        assertThat(utbetaling.mottaker).isEqualTo("fnr")
        assertThat(utbetaling.kontonummer).isEqualTo("kontonummer")
        assertThat(utbetaling.utbetalingsmetode).isEqualTo("pose med krølla femtilapper")
        assertThat(utbetaling.vilkar).isEmpty()
        assertThat(utbetaling.dokumentasjonkrav).isEmpty()
    }

    @Test
    suspend fun `utbetaling UTEN vedtakFattet`() {
        coEvery { jsonDigisosSokerService.get(any(), any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                        UTBETALING.withHendelsestidspunkt(tidspunkt_3),
                    ),
                )

        val model = service.createModel(defaultDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)

        assertThat(model.utbetalinger[0].belop).isEqualTo("1234.56")
    }

    @Test
    suspend fun `utbetaling kontonummer settes kun hvis annenMottaker er false`() {
        coEvery { jsonDigisosSokerService.get(any(), any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                        UTBETALING_ANNEN_MOTTAKER.withHendelsestidspunkt(tidspunkt_3),
                    ),
                )

        val model = service.createModel(defaultDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)

        assertThat(model.utbetalinger[0].belop).isEqualTo("1234.56")
        assertThat(model.utbetalinger[0].kontonummer).isNull()
        assertThat(model.utbetalinger[0].mottaker).isEqualTo("utleier")
    }
}
