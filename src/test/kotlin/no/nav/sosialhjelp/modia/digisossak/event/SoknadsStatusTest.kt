package no.nav.sosialhjelp.modia.digisossak.event

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.api.fiks.Tilleggsinformasjon
import no.nav.sosialhjelp.modia.digisossak.domain.SoknadsStatus
import no.nav.sosialhjelp.modia.digisossak.event.Titler.SOKNAD_FERDIGBEHANDLET
import no.nav.sosialhjelp.modia.digisossak.event.Titler.SOKNAD_MOTTATT
import no.nav.sosialhjelp.modia.digisossak.event.Titler.SOKNAD_SENDT
import no.nav.sosialhjelp.modia.digisossak.event.Titler.SOKNAD_UNDER_BEHANDLING
import no.nav.sosialhjelp.modia.navkontor.norg.NorgClient
import no.nav.sosialhjelp.modia.soknad.vedlegg.SoknadVedleggService
import no.nav.sosialhjelp.modia.soknad.vedlegg.VEDLEGG_KREVES_STATUS
import no.nav.sosialhjelp.modia.toLocalDateTime
import no.nav.sosialhjelp.modia.unixToLocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SoknadsStatusTest {
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
    suspend fun `soknadsStatus SENDT`() {
        coEvery { jsonDigisosSokerService.get(any(), any(), any(), any()) } returns null

        val model = service.createModel(defaultDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.SENDT)
        assertThat(model.historikk).hasSize(1)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tidspunkt).isEqualTo(unixToLocalDateTime(tidspunkt_soknad))
        assertThat(hendelse.tittel).isEqualTo(SOKNAD_SENDT)
        assertThat(hendelse.beskrivelse).contains("Søknaden med vedlegg er sendt til")
    }

    @Test
    suspend fun `soknadsStatus MOTTATT`() {
        coEvery { jsonDigisosSokerService.get(any(), any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                    ),
                )

        val model = service.createModel(defaultDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.MOTTATT)
        assertThat(model.historikk).hasSize(2)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tidspunkt).isEqualTo(tidspunkt_1.toLocalDateTime())
        assertThat(hendelse.tittel).isEqualTo(SOKNAD_MOTTATT)
        assertThat(hendelse.beskrivelse).contains("Søknaden med vedlegg er mottatt ved ")
    }

    @Test
    suspend fun `soknadsStatus MOTTATT papirsoknad`() {
        val papirsoknadDigisosSak =
            defaultDigisosSak.copy(
                originalSoknadNAV = null,
                tilleggsinformasjon =
                    Tilleggsinformasjon(
                        enhetsnummer = null,
                    ),
            )
        coEvery { jsonDigisosSokerService.get(any(), any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                    ),
                )

        val model = service.createModel(papirsoknadDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.MOTTATT)
        assertThat(model.historikk).hasSize(1)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tidspunkt).isEqualTo(tidspunkt_1.toLocalDateTime())
        assertThat(hendelse.tittel).isEqualTo(SOKNAD_MOTTATT)
        assertThat(hendelse.beskrivelse).contains("Søknaden med vedlegg er mottatt")
    }

    @Test
    suspend fun `soknadsStatus UNDER_BEHANDLING`() {
        coEvery { jsonDigisosSokerService.get(any(), any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                    ),
                )

        val model = service.createModel(defaultDigisosSak)

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
    suspend fun `soknadsStatus FERDIGBEHANDLET`() {
        coEvery { jsonDigisosSokerService.get(any(), any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                        SOKNADS_STATUS_FERDIGBEHANDLET.withHendelsestidspunkt(tidspunkt_3),
                    ),
                )

        val model = service.createModel(defaultDigisosSak)

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
