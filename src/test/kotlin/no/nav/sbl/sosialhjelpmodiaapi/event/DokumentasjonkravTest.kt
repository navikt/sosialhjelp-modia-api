package no.nav.sbl.sosialhjelpmodiaapi.event

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.client.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.DigisosSak
import no.nav.sbl.sosialhjelpmodiaapi.domain.SoknadsStatus
import no.nav.sbl.sosialhjelpmodiaapi.service.innsyn.InnsynService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DokumentasjonkravTest {

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
    fun `dokumentasjonskrav ETTER utbetaling`() {
        every { innsynService.hentJsonDigisosSoker(any(), any()) } returns
                JsonDigisosSoker()
                        .withAvsender(avsender)
                        .withVersion("123")
                        .withHendelser(listOf(
                                SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                                SAK1_VEDTAK_FATTET_INNVILGET.withHendelsestidspunkt(tidspunkt_3),
                                SOKNADS_STATUS_FERDIGBEHANDLET.withHendelsestidspunkt(tidspunkt_4),
                                UTBETALING.withHendelsestidspunkt(tidspunkt_5),
                                DOKUMENTASJONKRAV_OPPFYLT.withHendelsestidspunkt(tidspunkt_6)
                        ))

        val model = service.createModel(mockDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.FERDIGBEHANDLET)
        assertThat(model.saker).hasSize(1)
        assertThat(model.historikk).hasSize(5)

        assertThat(model.saker[0].utbetalinger).hasSize(1)
        val utbetaling = model.saker[0].utbetalinger[0]
        assertThat(utbetaling.dokumentasjonkrav).hasSize(1)
        assertThat(utbetaling.dokumentasjonkrav[0].referanse).isEqualTo(dokumentasjonkrav_ref_1)
        assertThat(utbetaling.dokumentasjonkrav[0].beskrivelse).isEqualTo("beskrivelse")
        assertThat(utbetaling.dokumentasjonkrav[0].oppfyllt).isEqualTo(true)
    }

    @Test
    fun `dokumentasjonkrav UTEN utbetaling`() {
        every { innsynService.hentJsonDigisosSoker(any(), any()) } returns
                JsonDigisosSoker()
                        .withAvsender(avsender)
                        .withVersion("123")
                        .withHendelser(listOf(
                                SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                                DOKUMENTASJONKRAV_OPPFYLT.withHendelsestidspunkt(tidspunkt_3)
                        ))

        val model = service.createModel(mockDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
        assertThat(model.saker).hasSize(0)
        assertThat(model.historikk).hasSize(3)
    }
}