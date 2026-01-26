package no.nav.sosialhjelp.modia.digisossak.event

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.modia.navkontor.norg.NorgClient
import no.nav.sosialhjelp.modia.soknad.vedlegg.InternalVedlegg
import no.nav.sosialhjelp.modia.soknad.vedlegg.SoknadVedleggService
import no.nav.sosialhjelp.modia.soknad.vedlegg.VEDLEGG_KREVES_STATUS
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class SoknadDokumentasjonkravTest {
    private val jsonDigisosSokerService: JsonDigisosSokerService = mockk()
    private val norgClient: NorgClient = mockk()
    private val soknadVedleggService: SoknadVedleggService = mockk()

    private val service = EventService(jsonDigisosSokerService, norgClient, soknadVedleggService)

    @BeforeEach
    fun init() {
        clearAllMocks()

        every { norgClient.hentNavEnhet(ENHETSNR)!!.navn } returns ENHETSNAVN

        resetHendelser()
    }

    @Test
    internal fun `skal legge til dokumentasjonkrav fra soknaden`() {
        every { jsonDigisosSokerService.get(any(), any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                        SAK1_SAKS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_3),
                    ),
                )
        every { soknadVedleggService.hentSoknadVedleggMedStatus(any(), VEDLEGG_KREVES_STATUS) } returns
            listOf(
                InternalVedlegg(
                    type = "statsborgerskap",
                    tilleggsinfo = "dokumentasjon",
                    innsendelsesfrist = null,
                    antallFiler = 1,
                    datoLagtTil = LocalDateTime.now(),
                    null,
                ),
            )

        val model = service.createModel(defaultDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.oppgaver).hasSize(1)
    }
}
