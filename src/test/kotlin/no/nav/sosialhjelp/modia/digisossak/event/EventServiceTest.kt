package no.nav.sosialhjelp.modia.digisossak.event

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.modia.digisossak.domain.SaksStatus
import no.nav.sosialhjelp.modia.digisossak.domain.SoknadsStatus
import no.nav.sosialhjelp.modia.digisossak.domain.UtfallVedtak
import no.nav.sosialhjelp.modia.digisossak.event.Titler.FORELOPIG_SVAR
import no.nav.sosialhjelp.modia.digisossak.event.Titler.SAK_FERDIGBEHANDLET
import no.nav.sosialhjelp.modia.digisossak.event.Titler.SOKNAD_UNDER_BEHANDLING
import no.nav.sosialhjelp.modia.navkontor.norg.NorgClient
import no.nav.sosialhjelp.modia.soknad.vedlegg.SoknadVedleggService
import no.nav.sosialhjelp.modia.soknad.vedlegg.VEDLEGG_KREVES_STATUS
import no.nav.sosialhjelp.modia.toLocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class EventServiceTest {

    private val jsonDigisosSokerService: JsonDigisosSokerService = mockk()
    private val norgClient: NorgClient = mockk()
    private val soknadVedleggService: SoknadVedleggService = mockk()

    private val service = EventService(jsonDigisosSokerService, norgClient, soknadVedleggService)

    private val mockDigisosSak: DigisosSak = mockk()

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

/* Test-caser:
 [x] ingen innsyn, ingen sendt soknad
 [x] ingen innsyn, sendt soknad -> status SENDT
 [x] status mottatt
 [x] status under behandling
 [x] status ferdigbehandlet
 [x] saksStatus uten vedtakFattet
 [x] saksStatus før vedtakFattet
 [x] vedtakFattet uten saksStatus
 [x] vedtakFattet før saksStatus
 [x] saksStatus med 2 vedtakFattet
 [x] dokumentasjonEtterspurt
 [x] dokumentasjonEtterspurt med tom dokumentliste
 [x] ingen dokumentasjonEtterspurt-hendelser
 [x] forelopigSvar
 [ ] forelopigSvar - flere caser?
 [ ] utbetaling
 [ ] utbetaling - flere caser?
 ...
 [ ] komplett case
*/

    @Test
    fun `ingen innsyn OG ingen soknad`() {
        every { jsonDigisosSokerService.get(any(), any(), any()) } returns null
        every { mockDigisosSak.tilleggsinformasjon?.enhetsnummer } returns null

        val model = service.createModel(mockDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.SENDT)
        assertThat(model.historikk).hasSize(1)
        assertThat(model.historikk[0].beskrivelse).isEqualTo("Søknaden med vedlegg er sendt til [Kan ikke hente NAV-kontor uten enhetsnummer].")
    }

    @Test
    fun `ingen innsyn `() {
        every { mockDigisosSak.digisosSoker } returns null
        every { jsonDigisosSokerService.get(any(), any(), null) } returns null

        val model = service.createModel(mockDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.historikk).hasSize(1)
    }

    @Nested
    inner class SaksStatusVedtakFattet {

        @Test
        fun `saksStatus UTEN vedtakFattet`() {
            every { jsonDigisosSokerService.get(any(), any(), any()) } returns
                JsonDigisosSoker()
                    .withAvsender(avsender)
                    .withVersion("123")
                    .withHendelser(
                        listOf(
                            SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                            SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                            SAK1_SAKS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_3)
                        )
                    )

            val model = service.createModel(mockDigisosSak)

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).hasSize(1)
            assertThat(model.historikk).hasSize(4)

            val sak = model.saker.last()
            assertThat(sak.saksStatus).isEqualTo(SaksStatus.UNDER_BEHANDLING)
            assertThat(sak.referanse).isEqualTo(referanse_1)
            assertThat(sak.tittel).isEqualTo(tittel_1)
            assertThat(sak.vedtak).isEmpty()
            assertThat(sak.utbetalinger).isEmpty()

            val hendelse = model.historikk[model.historikk.size - 2] // Second last
            assertThat(hendelse.tidspunkt).isEqualTo(tidspunkt_2.toLocalDateTime())
            assertThat(hendelse.tittel).isEqualTo(SOKNAD_UNDER_BEHANDLING)
            assertThat(hendelse.beskrivelse).isNull()
        }

        @Test
        fun `saksStatus UTEN tittel eller status`() {
            every { jsonDigisosSokerService.get(any(), any(), any()) } returns
                JsonDigisosSoker()
                    .withAvsender(avsender)
                    .withVersion("123")
                    .withHendelser(
                        listOf(
                            SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                            SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                            SAK1_UTEN_SAKS_STATUS_ELLER_TITTEL.withHendelsestidspunkt(tidspunkt_3)
                        )
                    )

            val model = service.createModel(mockDigisosSak)

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).hasSize(1)
            assertThat(model.historikk).hasSize(4)

            val sak = model.saker.last()
            assertThat(sak.saksStatus).isEqualTo(SaksStatus.UNDER_BEHANDLING)
            assertThat(sak.referanse).isEqualTo(referanse_1)
            assertThat(sak.tittel).isNull()
            assertThat(sak.vedtak).isEmpty()
            assertThat(sak.utbetalinger).isEmpty()

            val hendelse = model.historikk[model.historikk.size - 2] // Second last
            assertThat(hendelse.tidspunkt).isEqualTo(tidspunkt_2.toLocalDateTime())
            assertThat(hendelse.tittel).isEqualTo(SOKNAD_UNDER_BEHANDLING)
            assertThat(hendelse.beskrivelse).isNull()
        }

        @Test
        fun `saksStatus FOER vedtakFattet`() {
            every { jsonDigisosSokerService.get(any(), any(), any()) } returns
                JsonDigisosSoker()
                    .withAvsender(avsender)
                    .withVersion("123")
                    .withHendelser(
                        listOf(
                            SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                            SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                            SAK1_SAKS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_3),
                            SAK1_VEDTAK_FATTET_INNVILGET.withHendelsestidspunkt(tidspunkt_4)
                        )
                    )

            val model = service.createModel(mockDigisosSak)

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).hasSize(1)
            assertThat(model.historikk).hasSize(5)

            val sak = model.saker.last()
            assertThat(sak.saksStatus).isEqualTo(SaksStatus.UNDER_BEHANDLING)
            assertThat(sak.referanse).isEqualTo(referanse_1)
            assertThat(sak.tittel).isEqualTo(tittel_1)
            assertThat(sak.vedtak).hasSize(1)
            assertThat(sak.utbetalinger).isEmpty()

            val vedtak = sak.vedtak.last()
            assertThat(vedtak.utfall).isEqualTo(UtfallVedtak.INNVILGET)

            val hendelse = model.historikk.last()
            assertThat(hendelse.tidspunkt).isEqualTo(tidspunkt_4.toLocalDateTime())
            assertThat(hendelse.tittel).isEqualTo(SAK_FERDIGBEHANDLET)
            assertThat(hendelse.beskrivelse).contains("$tittel_1 er ferdigbehandlet")
        }

        @Test
        fun `vedtakFattet UTEN saksStatus`() {
            every { jsonDigisosSokerService.get(any(), any(), any()) } returns
                JsonDigisosSoker()
                    .withAvsender(avsender)
                    .withVersion("123")
                    .withHendelser(
                        listOf(
                            SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                            SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                            SAK1_VEDTAK_FATTET_INNVILGET.withHendelsestidspunkt(tidspunkt_3)
                        )
                    )

            val model = service.createModel(mockDigisosSak)

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).hasSize(1)
            assertThat(model.historikk).hasSize(4)

            val sak = model.saker.last()
            assertThat(sak.saksStatus).isEqualTo(SaksStatus.UNDER_BEHANDLING)
            assertThat(sak.referanse).isEqualTo(referanse_1)
            assertThat(sak.tittel).isEqualTo(SAK_DEFAULT_TITTEL)
            assertThat(sak.vedtak).hasSize(1)
            assertThat(sak.utbetalinger).isEmpty()

            val vedtak = sak.vedtak.last()
            assertThat(vedtak.utfall).isEqualTo(UtfallVedtak.INNVILGET)

            val hendelse = model.historikk.last()
            assertThat(hendelse.tidspunkt).isEqualTo(tidspunkt_3.toLocalDateTime())
            assertThat(hendelse.tittel).isEqualTo(SAK_FERDIGBEHANDLET)
            assertThat(hendelse.beskrivelse).contains("$SAK_DEFAULT_TITTEL er ferdigbehandlet")
        }

        @Test
        fun `vedtakFattet FOER saksStatus`() {
            every { jsonDigisosSokerService.get(any(), any(), any()) } returns
                JsonDigisosSoker()
                    .withAvsender(avsender)
                    .withVersion("123")
                    .withHendelser(
                        listOf(
                            SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                            SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                            SAK1_VEDTAK_FATTET_INNVILGET.withHendelsestidspunkt(tidspunkt_3),
                            SAK1_SAKS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_4)
                        )
                    )

            val model = service.createModel(mockDigisosSak)

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).hasSize(1)
            assertThat(model.historikk).hasSize(4)

            val sak = model.saker.last()
            assertThat(sak.saksStatus).isEqualTo(SaksStatus.UNDER_BEHANDLING)
            assertThat(sak.referanse).isEqualTo(referanse_1)
            assertThat(sak.tittel)
                .isEqualTo(tittel_1)
                .isNotEqualTo(SAK_DEFAULT_TITTEL)
            assertThat(sak.vedtak).hasSize(1)

            val vedtak = sak.vedtak.last()
            assertThat(vedtak.utfall).isEqualTo(UtfallVedtak.INNVILGET)

            val hendelse = model.historikk.last()
            assertThat(hendelse.tidspunkt).isEqualTo(tidspunkt_3.toLocalDateTime())
            assertThat(hendelse.tittel).isEqualTo(SAK_FERDIGBEHANDLET)
            assertThat(hendelse.beskrivelse).contains("$SAK_DEFAULT_TITTEL er ferdigbehandlet")
        }

        @Test
        fun `saksStatus med 2 vedtakFattet`() {
            every { jsonDigisosSokerService.get(any(), any(), any()) } returns
                JsonDigisosSoker()
                    .withAvsender(avsender)
                    .withVersion("123")
                    .withHendelser(
                        listOf(
                            SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                            SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                            SAK1_SAKS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_3),
                            SAK1_VEDTAK_FATTET_INNVILGET.withHendelsestidspunkt(tidspunkt_4),
                            SAK1_VEDTAK_FATTET_AVSLATT.withHendelsestidspunkt(tidspunkt_5)
                        )
                    )

            val model = service.createModel(mockDigisosSak)

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).hasSize(1)
            assertThat(model.historikk).hasSize(6)

            val sak = model.saker.last()
            assertThat(sak.saksStatus).isEqualTo(SaksStatus.UNDER_BEHANDLING)
            assertThat(sak.referanse).isEqualTo(referanse_1)
            assertThat(sak.tittel).isEqualTo(tittel_1)
            assertThat(sak.vedtak).hasSize(2)

            val vedtak = sak.vedtak[0]
            assertThat(vedtak.utfall).isEqualTo(UtfallVedtak.INNVILGET)

            val vedtak2 = sak.vedtak[1]
            assertThat(vedtak2.utfall).isEqualTo(UtfallVedtak.AVSLATT)
        }

        @Test
        fun `saksStatus uten tittel eller status med vedtakFattet uten utfall`() {
            every { jsonDigisosSokerService.get(any(), any(), any()) } returns
                JsonDigisosSoker()
                    .withAvsender(avsender)
                    .withVersion("123")
                    .withHendelser(
                        listOf(
                            SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                            SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                            SAK1_UTEN_SAKS_STATUS_ELLER_TITTEL.withHendelsestidspunkt(tidspunkt_3),
                            SAK1_VEDTAK_FATTET_UTEN_UTFALL.withHendelsestidspunkt(tidspunkt_4)
                        )
                    )

            val model = service.createModel(mockDigisosSak)

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).hasSize(1)
            assertThat(model.historikk).hasSize(5)

            val sak = model.saker.last()
            assertThat(sak.saksStatus).isEqualTo(SaksStatus.UNDER_BEHANDLING)
            assertThat(sak.referanse).isEqualTo(referanse_1)
            assertThat(sak.tittel).isNull()
            assertThat(sak.vedtak).hasSize(1)

            val vedtak = sak.vedtak[0]
            assertThat(vedtak.utfall).isNull()

            val hendelse = model.historikk.last()
            assertThat(hendelse.tidspunkt).isEqualTo(tidspunkt_4.toLocalDateTime())
            assertThat(hendelse.tittel).isEqualTo(SAK_FERDIGBEHANDLET)
            assertThat(hendelse.beskrivelse).contains("$SAK_DEFAULT_TITTEL er ferdigbehandlet")
        }
    }

    @Test
    fun `forelopigSvar skal gi historikk`() {
        every { jsonDigisosSokerService.get(any(), any(), any()) } returns
            JsonDigisosSoker()
                .withAvsender(avsender)
                .withVersion("123")
                .withHendelser(
                    listOf(
                        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                        SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                        FORELOPIGSVAR.withHendelsestidspunkt(tidspunkt_3)
                    )
                )

        val model = service.createModel(mockDigisosSak)

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
        assertThat(model.saker).isEmpty()
        assertThat(model.historikk).hasSize(4)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tidspunkt).isEqualTo(tidspunkt_3.toLocalDateTime())
        assertThat(hendelse.tittel).isEqualTo(FORELOPIG_SVAR)
    }
}
