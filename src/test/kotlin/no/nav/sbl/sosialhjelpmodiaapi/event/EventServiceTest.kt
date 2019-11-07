package no.nav.sbl.sosialhjelpmodiaapi.event

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.digisos.soker.*
import no.nav.sbl.soknadsosialhjelp.digisos.soker.filreferanse.JsonDokumentlagerFilreferanse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.filreferanse.JsonSvarUtFilreferanse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.*
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.sosialhjelpmodiaapi.domain.*
import no.nav.sbl.sosialhjelpmodiaapi.innsyn.InnsynService
import no.nav.sbl.sosialhjelpmodiaapi.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.saksstatus.DEFAULT_TITTEL
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime
import no.nav.sbl.sosialhjelpmodiaapi.unixToLocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


internal class EventServiceTest {

    private val innsynService: InnsynService = mockk()
    private val norgClient: NorgClient = mockk()

    private val service = EventService(innsynService, norgClient)

    private val mockDigisosSak: DigisosSak = mockk()
    private val mockJsonDigisosSoker: JsonDigisosSoker = mockk()
    private val mockJsonSoknad: JsonSoknad = mockk()
    private val mockNavEnhet: NavEnhet = mockk()

    private val soknadsmottaker = "The Office"
    private val enhetsnr = "2317"

    private val tittel_1 = "tittel"
    private val tittel_2 = "tittel2"

    private val referanse_1 = "sak1"
    private val referanse_2 = "sak2"

    private val dokumentlagerId_1 = "1"
    private val dokumentlagerId_2 = "2"
    private val svarUtId = "42"
    private val svarUtNr = 42

    private val dokumenttype = "dokumentasjonstype"
    private val tilleggsinfo = "ekstra info"

    private val navKontor = "1337"

    private val now = ZonedDateTime.now()

    private val tidspunkt_soknad = now.minusHours(11).toEpochSecond() * 1000L
    private val tidspunkt_1 = now.minusHours(10).format(DateTimeFormatter.ISO_DATE_TIME)
    private val tidspunkt_2 = now.minusHours(9).format(DateTimeFormatter.ISO_DATE_TIME)
    private val tidspunkt_3 = now.minusHours(8).format(DateTimeFormatter.ISO_DATE_TIME)
    private val tidspunkt_4 = now.minusHours(7).format(DateTimeFormatter.ISO_DATE_TIME)
    private val tidspunkt_5 = now.minusHours(6).format(DateTimeFormatter.ISO_DATE_TIME)

    private val innsendelsesfrist = now.plusDays(7).format(DateTimeFormatter.ISO_DATE_TIME)

    private val avsender = JsonAvsender().withSystemnavn("test").withSystemversjon("123")

    @BeforeEach
    fun init() {
        clearMocks(innsynService, mockJsonDigisosSoker, mockJsonSoknad)
        every { mockDigisosSak.fiksDigisosId } returns "123"
        every { mockDigisosSak.digisosSoker?.metadata } returns "some id"
        every { mockDigisosSak.originalSoknadNAV?.metadata } returns "some other id"
        every { mockDigisosSak.originalSoknadNAV?.timestampSendt } returns tidspunkt_soknad
        every { mockJsonSoknad.mottaker.navEnhetsnavn } returns soknadsmottaker
        every { mockJsonSoknad.mottaker.enhetsnummer } returns enhetsnr
        every { innsynService.hentOriginalSoknad(any(), any(), any()) } returns mockJsonSoknad
        every { norgClient.hentNavEnhet(enhetsnr) } returns mockNavEnhet

        resetHendelser()
    }

/* Test-caser:
 [x] ingen innsyn, ingen sendt soknad
 [x] ingen innsyn, sendt soknad -> status SENDT
 [x] status mottatt
 [x] status under behandling
 [x] status ferdig behandlet
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
        every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns null
        every { innsynService.hentOriginalSoknad(any(), any(), any()) } returns null

        val model = service.createModel(mockDigisosSak, "token")

        assertThat(model).isNotNull
        assertThat(model.status).isNull()
        assertThat(model.historikk).hasSize(0)
    }

    @Test
    fun `ingen innsyn `() {
        every { mockDigisosSak.digisosSoker } returns null
        every { innsynService.hentJsonDigisosSoker(any(), null, any()) } returns null

        val model = service.createModel(mockDigisosSak, "token")

        assertThat(model).isNotNull
        assertThat(model.historikk).hasSize(1)
    }

    @Nested
    inner class SoknadStatus {
        @Test
        fun `soknadsStatus SENDT`() {
            every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns null

            val model = service.createModel(mockDigisosSak, "token")

            assertThat(model).isNotNull
            assertThat(model.historikk).hasSize(1)

            val hendelse = model.historikk.last()
            assertThat(hendelse.tidspunkt).isEqualTo(unixToLocalDateTime(tidspunkt_soknad))
            assertThat(hendelse.tittel).contains("Søknaden med vedlegg er sendt til")
        }

        @Test
        fun `soknadsStatus MOTTATT`() {
            every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns
                    JsonDigisosSoker()
                            .withAvsender(avsender)
                            .withVersion("123")
                            .withHendelser(listOf(
                                    SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1)
                            ))

            val model = service.createModel(mockDigisosSak, "token")

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.MOTTATT)
            assertThat(model.historikk).hasSize(2)

            val hendelse = model.historikk.last()
            assertThat(hendelse.tidspunkt).isEqualTo(toLocalDateTime(tidspunkt_1))
            assertThat(hendelse.tittel).contains("Søknaden med vedlegg er mottatt hos ")
        }

        @Test
        fun `soknadsStatus UNDER_BEHANDLING`() {
            every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns
                    JsonDigisosSoker()
                            .withAvsender(avsender)
                            .withVersion("123")
                            .withHendelser(listOf(
                                    SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                    SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2)
                            ))

            val model = service.createModel(mockDigisosSak, "token")

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).isEmpty()
            assertThat(model.historikk).hasSize(3)

            val hendelse = model.historikk.last()
            assertThat(hendelse.tidspunkt).isEqualTo(toLocalDateTime(tidspunkt_2))
            assertThat(hendelse.tittel).contains("Søknaden er under behandling")
        }

        @Test
        fun `soknadsStatus FERDIGBEHANDLET`() {
            every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns
                    JsonDigisosSoker()
                            .withAvsender(avsender)
                            .withVersion("123")
                            .withHendelser(listOf(
                                    SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                    SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                                    SOKNADS_STATUS_FERDIGBEHANDLET.withHendelsestidspunkt(tidspunkt_3)
                            ))

            val model = service.createModel(mockDigisosSak, "token")

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.FERDIGBEHANDLET)
            assertThat(model.saker).isEmpty()
            assertThat(model.historikk).hasSize(4)

            val hendelse = model.historikk.last()
            assertThat(hendelse.tidspunkt).isEqualTo(toLocalDateTime(tidspunkt_3))
            assertThat(hendelse.tittel).contains("Søknaden er ferdig behandlet")
        }
    }

    @Nested
    inner class SaksStatusVedtakFattet {

        @Test
        fun `saksStatus UTEN vedtakFattet`() {
            every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns
                    JsonDigisosSoker()
                            .withAvsender(avsender)
                            .withVersion("123")
                            .withHendelser(listOf(
                                    SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                    SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                                    SAK1_SAKS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_3)
                            ))

            val model = service.createModel(mockDigisosSak, "token")

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).hasSize(1)
            assertThat(model.historikk).hasSize(3)

            val sak = model.saker.last()
            assertThat(sak.saksStatus).isEqualTo(SaksStatus.UNDER_BEHANDLING)
            assertThat(sak.referanse).isEqualTo(referanse_1)
            assertThat(sak.tittel).isEqualTo(tittel_1)
            assertThat(sak.vedtak).isEmpty()
            assertThat(sak.utbetalinger).isEmpty()

            val hendelse = model.historikk.last()
            assertThat(hendelse.tidspunkt).isEqualTo(toLocalDateTime(tidspunkt_2))
            assertThat(hendelse.tittel).contains("Søknaden er under behandling")
        }

        @Test
        fun `saksStatus UTEN tittel eller status`() {
            every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns
                    JsonDigisosSoker()
                            .withAvsender(avsender)
                            .withVersion("123")
                            .withHendelser(listOf(
                                    SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                    SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                                    SAK1_UTEN_SAKS_STATUS_ELLER_TITTEL.withHendelsestidspunkt(tidspunkt_3)
                            ))

            val model = service.createModel(mockDigisosSak, "token")

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).hasSize(1)
            assertThat(model.historikk).hasSize(3)

            val sak = model.saker.last()
            assertThat(sak.saksStatus).isEqualTo(SaksStatus.UNDER_BEHANDLING)
            assertThat(sak.referanse).isEqualTo(referanse_1)
            assertThat(sak.tittel).isNull()
            assertThat(sak.vedtak).isEmpty()
            assertThat(sak.utbetalinger).isEmpty()

            val hendelse = model.historikk.last()
            assertThat(hendelse.tidspunkt).isEqualTo(toLocalDateTime(tidspunkt_2))
            assertThat(hendelse.tittel).contains("Søknaden er under behandling")
        }

        @Test
        fun `saksStatus FØR vedtakFattet`() {
            every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns
                    JsonDigisosSoker()
                            .withAvsender(avsender)
                            .withVersion("123")
                            .withHendelser(listOf(
                                    SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                    SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                                    SAK1_SAKS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_3),
                                    SAK1_VEDTAK_FATTET_INNVILGET.withHendelsestidspunkt(tidspunkt_4)
                            ))

            val model = service.createModel(mockDigisosSak, "token")

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).hasSize(1)
            assertThat(model.historikk).hasSize(4)

            val sak = model.saker.last()
            assertThat(sak.saksStatus).isEqualTo(SaksStatus.UNDER_BEHANDLING)
            assertThat(sak.referanse).isEqualTo(referanse_1)
            assertThat(sak.tittel).isEqualTo(tittel_1)
            assertThat(sak.vedtak).hasSize(1)
            assertThat(sak.utbetalinger).isEmpty()

            val vedtak = sak.vedtak.last()
            assertThat(vedtak.utfall).isEqualTo(UtfallVedtak.INNVILGET)

            val hendelse = model.historikk.last()
            assertThat(hendelse.tidspunkt).isEqualTo(toLocalDateTime(tidspunkt_4))
            assertThat(hendelse.tittel).contains("$tittel_1 er ferdig behandlet")
        }

        @Test
        fun `vedtakFattet UTEN saksStatus`() {
            every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns
                    JsonDigisosSoker()
                            .withAvsender(avsender)
                            .withVersion("123")
                            .withHendelser(listOf(
                                    SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                    SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                                    SAK1_VEDTAK_FATTET_INNVILGET.withHendelsestidspunkt(tidspunkt_3)
                            ))

            val model = service.createModel(mockDigisosSak, "token")

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).hasSize(1)
            assertThat(model.historikk).hasSize(4)

            val sak = model.saker.last()
            assertThat(sak.saksStatus).isNull()
            assertThat(sak.referanse).isEqualTo(referanse_1)
            assertThat(sak.tittel).isNull()
            assertThat(sak.vedtak).hasSize(1)
            assertThat(sak.utbetalinger).isEmpty()

            val vedtak = sak.vedtak.last()
            assertThat(vedtak.utfall).isEqualTo(UtfallVedtak.INNVILGET)

            val hendelse = model.historikk.last()
            assertThat(hendelse.tidspunkt).isEqualTo(toLocalDateTime(tidspunkt_3))
            assertThat(hendelse.tittel).contains("$DEFAULT_TITTEL er ferdig behandlet")
        }

        @Test
        fun `vedtakFattet FØR saksStatus`() {
            every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns
                    JsonDigisosSoker()
                            .withAvsender(avsender)
                            .withVersion("123")
                            .withHendelser(listOf(
                                    SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                    SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                                    SAK1_VEDTAK_FATTET_INNVILGET.withHendelsestidspunkt(tidspunkt_3),
                                    SAK1_SAKS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_4)
                            ))

            val model = service.createModel(mockDigisosSak, "token")

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).hasSize(1)
            assertThat(model.historikk).hasSize(4)

            val sak = model.saker.last()
            assertThat(sak.saksStatus).isEqualTo(SaksStatus.UNDER_BEHANDLING)
            assertThat(sak.referanse).isEqualTo(referanse_1)
            assertThat(sak.tittel)
                    .isEqualTo(tittel_1)
                    .isNotEqualTo(DEFAULT_TITTEL)
            assertThat(sak.vedtak).hasSize(1)

            val vedtak = sak.vedtak.last()
            assertThat(vedtak.utfall).isEqualTo(UtfallVedtak.INNVILGET)

            val hendelse = model.historikk.last()
            assertThat(hendelse.tidspunkt).isEqualTo(toLocalDateTime(tidspunkt_3))
            assertThat(hendelse.tittel).contains("$DEFAULT_TITTEL er ferdig behandlet")
        }

        @Test
        fun `saksStatus med 2 vedtakFattet`() {
            every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns
                    JsonDigisosSoker()
                            .withAvsender(avsender)
                            .withVersion("123")
                            .withHendelser(listOf(
                                    SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                    SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                                    SAK1_SAKS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_3),
                                    SAK1_VEDTAK_FATTET_INNVILGET.withHendelsestidspunkt(tidspunkt_4),
                                    SAK1_VEDTAK_FATTET_AVSLATT.withHendelsestidspunkt(tidspunkt_5)
                            ))

            val model = service.createModel(mockDigisosSak, "token")

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).hasSize(1)
            assertThat(model.historikk).hasSize(5)

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
            every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns
                    JsonDigisosSoker()
                            .withAvsender(avsender)
                            .withVersion("123")
                            .withHendelser(listOf(
                                    SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                    SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                                    SAK1_UTEN_SAKS_STATUS_ELLER_TITTEL.withHendelsestidspunkt(tidspunkt_3),
                                    SAK1_VEDTAK_FATTET_UTEN_UTFALL.withHendelsestidspunkt(tidspunkt_4)
                            ))

            val model = service.createModel(mockDigisosSak, "token")

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).hasSize(1)
            assertThat(model.historikk).hasSize(4)

            val sak = model.saker.last()
            assertThat(sak.saksStatus).isEqualTo(SaksStatus.UNDER_BEHANDLING)
            assertThat(sak.referanse).isEqualTo(referanse_1)
            assertThat(sak.tittel).isNull()
            assertThat(sak.vedtak).hasSize(1)

            val vedtak = sak.vedtak[0]
            assertThat(vedtak.utfall).isNull()

            val hendelse = model.historikk.last()
            assertThat(hendelse.tidspunkt).isEqualTo(toLocalDateTime(tidspunkt_4))
            assertThat(hendelse.tittel).contains("$DEFAULT_TITTEL er ferdig behandlet")
        }
    }


    @Nested
    inner class DokumentasjonEtterspurt {

        @Test
        fun `dokumentasjonEtterspurt skal gi oppgaver og historikk`() {
            every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns
                    JsonDigisosSoker()
                            .withAvsender(avsender)
                            .withVersion("123")
                            .withHendelser(listOf(
                                    SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                    SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                                    DOKUMENTASJONETTERSPURT.withHendelsestidspunkt(tidspunkt_3)
                            ))

            val model = service.createModel(mockDigisosSak, "token")

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).isEmpty()
            assertThat(model.oppgaver).hasSize(1)
            assertThat(model.historikk).hasSize(4)

            val oppgave = model.oppgaver.last()
            assertThat(oppgave.tittel).isEqualTo(dokumenttype)
            assertThat(oppgave.tilleggsinfo).isEqualTo(tilleggsinfo)
            assertThat(oppgave.innsendelsesfrist).isEqualTo(toLocalDateTime(innsendelsesfrist))
            assertThat(oppgave.erFraInnsyn).isEqualTo(true)

            val hendelse = model.historikk.last()
            assertThat(hendelse.tidspunkt).isEqualTo(toLocalDateTime(tidspunkt_3))
            assertThat(hendelse.tittel).contains("Veileder har oppdatert dine dokumentasjonskrav: 1 vedlegg mangler")
        }

        @Test
        fun `dokumentasjonEtterspurt skal gi egen historikkmelding og ikke url eller oppgaver dersom det dokumentlisten er tom`() {
            every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns
                    JsonDigisosSoker()
                            .withAvsender(avsender)
                            .withVersion("123")
                            .withHendelser(listOf(
                                    SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                    SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                                    DOKUMENTASJONETTERSPURT_TOM_DOKUMENT_LISTE.withHendelsestidspunkt(tidspunkt_3)
                            ))

            val model = service.createModel(mockDigisosSak, "token")

            assertThat(model).isNotNull
            assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
            assertThat(model.saker).isEmpty()
            assertThat(model.oppgaver).hasSize(0)
            assertThat(model.historikk).hasSize(4)

            val hendelse = model.historikk.last()
            assertThat(hendelse.tidspunkt).isEqualTo(toLocalDateTime(tidspunkt_3))
            assertThat(hendelse.tittel).contains("Veileder har oppdatert dine dokumentasjonskrav: Ingen vedlegg mangler")
        }
    }

    @Test
    fun `forelopigSvar skal gi historikk`() {
        every { innsynService.hentJsonDigisosSoker(any(), any(), any()) } returns
                JsonDigisosSoker()
                        .withAvsender(avsender)
                        .withVersion("123")
                        .withHendelser(listOf(
                                SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(tidspunkt_1),
                                SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(tidspunkt_2),
                                FORELOPIGSVAR.withHendelsestidspunkt(tidspunkt_3)
                        ))

        val model = service.createModel(mockDigisosSak, "token")

        assertThat(model).isNotNull
        assertThat(model.status).isEqualTo(SoknadsStatus.UNDER_BEHANDLING)
        assertThat(model.saker).isEmpty()
        assertThat(model.historikk).hasSize(4)

        val hendelse = model.historikk.last()
        assertThat(hendelse.tidspunkt).isEqualTo(toLocalDateTime(tidspunkt_3))
        assertThat(hendelse.tittel).contains("Du har fått et brev om saksbehandlingstiden for søknaden din")
    }

    private fun resetHendelser() {
        SOKNADS_STATUS_MOTTATT.withHendelsestidspunkt(null)
        SOKNADS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(null)
        SOKNADS_STATUS_FERDIGBEHANDLET.withHendelsestidspunkt(null)
        TILDELT_NAV_KONTOR.withHendelsestidspunkt(null)
        SAK1_SAKS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(null)
        SAK1_SAKS_STATUS_IKKEINNSYN.withHendelsestidspunkt(null)
        SAK2_SAKS_STATUS_UNDERBEHANDLING.withHendelsestidspunkt(null)
        SAK1_VEDTAK_FATTET_INNVILGET.withHendelsestidspunkt(null)
        SAK1_VEDTAK_FATTET_AVSLATT.withHendelsestidspunkt(null)
        SAK2_VEDTAK_FATTET.withHendelsestidspunkt(null)
        DOKUMENTASJONETTERSPURT.withHendelsestidspunkt(null)
        FORELOPIGSVAR.withHendelsestidspunkt(null)
    }

    private val DOKUMENTLAGER_1 = JsonDokumentlagerFilreferanse().withType(JsonFilreferanse.Type.DOKUMENTLAGER).withId(dokumentlagerId_1)
    private val DOKUMENTLAGER_2 = JsonDokumentlagerFilreferanse().withType(JsonFilreferanse.Type.DOKUMENTLAGER).withId(dokumentlagerId_2)
    private val SVARUT_1 = JsonSvarUtFilreferanse().withType(JsonFilreferanse.Type.DOKUMENTLAGER).withId(svarUtId).withNr(svarUtNr)

    private val SOKNADS_STATUS_MOTTATT = JsonSoknadsStatus()
            .withType(JsonHendelse.Type.SOKNADS_STATUS)
            .withStatus(JsonSoknadsStatus.Status.MOTTATT)

    private val SOKNADS_STATUS_UNDERBEHANDLING = JsonSoknadsStatus()
            .withType(JsonHendelse.Type.SOKNADS_STATUS)
            .withStatus(JsonSoknadsStatus.Status.UNDER_BEHANDLING)

    private val SOKNADS_STATUS_FERDIGBEHANDLET = JsonSoknadsStatus()
            .withType(JsonHendelse.Type.SOKNADS_STATUS)
            .withStatus(JsonSoknadsStatus.Status.FERDIGBEHANDLET)

    private val TILDELT_NAV_KONTOR = JsonTildeltNavKontor()
            .withType(JsonHendelse.Type.TILDELT_NAV_KONTOR)
            .withNavKontor(navKontor)

    private val SAK1_SAKS_STATUS_UNDERBEHANDLING = JsonSaksStatus()
            .withType(JsonHendelse.Type.SAKS_STATUS)
            .withStatus(JsonSaksStatus.Status.UNDER_BEHANDLING)
            .withTittel(tittel_1)
            .withReferanse(referanse_1)

    private val SAK1_UTEN_SAKS_STATUS_ELLER_TITTEL = JsonSaksStatus()
            .withType(JsonHendelse.Type.SAKS_STATUS)
            .withReferanse(referanse_1)

    private val SAK1_SAKS_STATUS_IKKEINNSYN = JsonSaksStatus()
            .withType(JsonHendelse.Type.SAKS_STATUS)
            .withStatus(JsonSaksStatus.Status.IKKE_INNSYN)
            .withTittel(tittel_1)
            .withReferanse(referanse_1)

    private val SAK2_SAKS_STATUS_UNDERBEHANDLING = JsonSaksStatus()
            .withType(JsonHendelse.Type.SAKS_STATUS)
            .withStatus(JsonSaksStatus.Status.UNDER_BEHANDLING)
            .withTittel(tittel_2)
            .withReferanse(referanse_2)

    private val SAK1_VEDTAK_FATTET_INNVILGET = JsonVedtakFattet()
            .withType(JsonHendelse.Type.VEDTAK_FATTET)
            .withSaksreferanse(referanse_1)
            .withVedtaksfil(JsonVedtaksfil().withReferanse(DOKUMENTLAGER_1))
            .withUtfall(JsonVedtakFattet.Utfall.INNVILGET)

    private val SAK1_VEDTAK_FATTET_UTEN_UTFALL = JsonVedtakFattet()
            .withType(JsonHendelse.Type.VEDTAK_FATTET)
            .withSaksreferanse(referanse_1)
            .withVedtaksfil(JsonVedtaksfil().withReferanse(DOKUMENTLAGER_1))

    private val SAK1_VEDTAK_FATTET_AVSLATT = JsonVedtakFattet()
            .withType(JsonHendelse.Type.VEDTAK_FATTET)
            .withSaksreferanse(referanse_1)
            .withVedtaksfil(JsonVedtaksfil().withReferanse(DOKUMENTLAGER_2))
            .withUtfall(JsonVedtakFattet.Utfall.AVSLATT)

    private val SAK2_VEDTAK_FATTET = JsonVedtakFattet()
            .withType(JsonHendelse.Type.VEDTAK_FATTET)
            .withSaksreferanse(referanse_2)
            .withVedtaksfil(JsonVedtaksfil().withReferanse(SVARUT_1))
            .withUtfall(JsonVedtakFattet.Utfall.INNVILGET)

    private val DOKUMENTASJONETTERSPURT = JsonDokumentasjonEtterspurt()
            .withType(JsonHendelse.Type.DOKUMENTASJON_ETTERSPURT)
            .withDokumenter(mutableListOf(JsonDokumenter().withInnsendelsesfrist(innsendelsesfrist).withDokumenttype(dokumenttype).withTilleggsinformasjon(tilleggsinfo)))
            .withForvaltningsbrev(JsonForvaltningsbrev().withReferanse(DOKUMENTLAGER_1))

    private val DOKUMENTASJONETTERSPURT_TOM_DOKUMENT_LISTE = JsonDokumentasjonEtterspurt()
            .withType(JsonHendelse.Type.DOKUMENTASJON_ETTERSPURT)
            .withForvaltningsbrev(JsonForvaltningsbrev().withReferanse(DOKUMENTLAGER_1))

    private val FORELOPIGSVAR = JsonForelopigSvar()
            .withType(JsonHendelse.Type.FORELOPIG_SVAR)
            .withForvaltningsbrev(JsonForvaltningsbrev().withReferanse(SVARUT_1))
}