package no.nav.sosialhjelp.modia.digisossak.event

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.domain.SoknadsStatus
import no.nav.sosialhjelp.modia.digisossak.event.Titler.DOKUMENTASJONSKRAV
import no.nav.sosialhjelp.modia.navkontor.norg.NorgClient
import no.nav.sosialhjelp.modia.soknad.vedlegg.SoknadVedleggService
import no.nav.sosialhjelp.modia.soknad.vedlegg.VEDLEGG_KREVES_STATUS
import no.nav.sosialhjelp.modia.toLocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class DokumentasjonEtterspurtTest {
    private val jsonDigisosSokerService: JsonDigisosSokerService = mockk()
    private val norgClient: NorgClient = mockk()
    private val soknadVedleggService: SoknadVedleggService = mockk()

    private val service = EventService(jsonDigisosSokerService, norgClient, soknadVedleggService)

    private val vedleggKrevesDokumenttype = "faktura"
    private val vedleggKrevesTilleggsinfo = "strom"

    @BeforeEach
    fun init() {
        clearAllMocks()
        every { norgClient.hentNavEnhet(ENHETSNR)!!.navn } returns ENHETSNAVN

        coEvery { soknadVedleggService.hentSoknadVedleggMedStatus(any(), VEDLEGG_KREVES_STATUS) } returns emptyList()

        resetHendelser()
    }

    @Test
    suspend fun `dokumentliste er satt OG vedtaksbrev er satt - skal gi oppgaver og historikk`() {
        coEvery { jsonDigisosSokerService.get(any(), any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                        DOKUMENTASJONETTERSPURT.withHendelsestidspunkt(tidspunkt_3),
                    ),
                )

        val model = service.createModel(defaultDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
        assertThat(model.saker).isEmpty()
        assertThat(model.oppgaver).hasSize(1)
        assertThat(model.historikk).hasSize(4)

        val oppgave = model.oppgaver.last()
        assertThat(oppgave.tittel).isEqualTo(DOKUMENT_TYPE)
        assertThat(oppgave.tilleggsinfo).isEqualTo(TILLEGGSINFO)
        assertThat(oppgave.innsendelsesfrist).isEqualTo(innsendelsesfrist.toLocalDateTime())
        assertThat(oppgave.erFraInnsyn).isEqualTo(true)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tidspunkt).isEqualTo(tidspunkt_3.toLocalDateTime())
        assertThat(hendelse.tittel).isEqualTo(DOKUMENTASJONSKRAV)
    }

    @Test
    internal suspend fun `dokumentliste er satt OG forvaltningsbrev mangler - skal gi oppgaver men ikke historikk`() {
        coEvery { jsonDigisosSokerService.get(any(), any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                        DOKUMENTASJONETTERSPURT_UTEN_FORVALTNINGSBREV.withHendelsestidspunkt(tidspunkt_3),
                    ),
                )

        val model = service.createModel(defaultDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
        assertThat(model.saker).isEmpty()
        assertThat(model.oppgaver).hasSize(1)
        assertThat(model.historikk).hasSize(3)

        val oppgave = model.oppgaver.last()
        assertThat(oppgave.tittel).isEqualTo(DOKUMENT_TYPE)
        assertThat(oppgave.tilleggsinfo).isEqualTo(TILLEGGSINFO)
        assertThat(oppgave.innsendelsesfrist).isEqualTo(innsendelsesfrist.toLocalDateTime())
        assertThat(oppgave.erFraInnsyn).isEqualTo(true)
    }

    @Test
    suspend fun `dokumentliste er tom OG forvaltningsbrev er satt - skal verken gi oppgaver eller historikk`() {
        coEvery { jsonDigisosSokerService.get(any(), any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                        DOKUMENTASJONETTERSPURT_TOM_DOKUMENT_LISTE.withHendelsestidspunkt(tidspunkt_3),
                    ),
                )

        val model = service.createModel(defaultDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
        assertThat(model.saker).isEmpty()
        assertThat(model.oppgaver).hasSize(0)
        assertThat(model.historikk).hasSize(3)
    }

    @Disabled("fixme - oppgaver fra søknad ikke inkludert (enda)")
    @Test
    suspend fun `oppgaver skal hentes fra soknaden dersom det ikke finnes dokumentasjonEtterspurt`() {
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
        assertThat(model.oppgaver).hasSize(1)
        assertThat(model.historikk).hasSize(3)

        val oppgave = model.oppgaver.last()
        assertThat(oppgave.tittel).isEqualTo(vedleggKrevesDokumenttype)
        assertThat(oppgave.tilleggsinfo).isEqualTo(vedleggKrevesTilleggsinfo)
        assertThat(oppgave.innsendelsesfrist).isNull()
        assertThat(oppgave.erFraInnsyn).isEqualTo(false)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tidspunkt).isEqualTo(tidspunkt_2.toLocalDateTime())
        assertThat(hendelse.tittel).contains("Søknaden er under behandling")
    }
}
