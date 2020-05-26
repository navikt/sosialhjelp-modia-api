package no.nav.sbl.sosialhjelpmodiaapi.service.vedlegg

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelpmodiaapi.domain.*
import no.nav.sbl.sosialhjelpmodiaapi.event.EventService
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

internal class VedleggServiceTest {

    private val fiksClient: FiksClient = mockk()
    private val eventService: EventService = mockk()

    private val service = VedleggService(fiksClient, eventService)

    private val mockDigisosSak: DigisosSak = mockk()
    private val mockJsonVedleggSpesifikasjon: JsonVedleggSpesifikasjon = mockk()

    private val zoneIdOslo = ZoneId.of("Europe/Oslo")

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { fiksClient.hentDigisosSak(any(), any()) } returns mockDigisosSak
        every { mockDigisosSak.originalSoknadNAV } returns originalSoknad
        every { mockDigisosSak.ettersendtInfoNAV?.ettersendelser } returns ettersendelser

        every { mockJsonVedleggSpesifikasjon.vedlegg } returns emptyList()

        every { fiksClient.hentDokument(any(), vedleggMetadata_soknad_1, any(), "token") } returns soknadVedleggSpesifikasjon
        every { fiksClient.hentDokument(any(), vedleggMetadata_soknad_2, any(), "token") } returns soknadVedleggSpesifikasjonMedStatusKrevesOgLastetOpp
        every { fiksClient.hentDokument(any(), vedleggMetadata_ettersendelse_1, any(), "token") } returns ettersendteVedleggSpesifikasjon_1
        every { fiksClient.hentDokument(any(), vedleggMetadata_ettersendelse_2, any(), "token") } returns ettersendteVedleggSpesifikasjon_2
        every { fiksClient.hentDokument(any(), vedleggMetadata_ettersendelse_3, any(), "token") } returns ettersendteVedleggSpesifikasjon_3
        every { fiksClient.hentDokument(any(), vedleggMetadata_ettersendelse_4, any(), "token") } returns ettersendteVedleggSpesifikasjon_4
    }

    @Test
    fun `skal returnere emptylist hvis soknad har null vedlegg og ingen ettersendelser finnes`() {
        val model = InternalDigisosSoker()

        every { eventService.createModel(any(), any()) } returns model
        every { fiksClient.hentDokument(any(), vedleggMetadata_soknad_1, any(), any()) } returns mockJsonVedleggSpesifikasjon

        every { mockDigisosSak.ettersendtInfoNAV?.ettersendelser } returns emptyList()

        val list = service.hentAlleOpplastedeVedlegg(id, "token")

        assertThat(list).isEmpty()
    }

    @Test
    fun `skal kun returnere soknadens vedlegg hvis ingen ettersendelser finnes`() {
        val model = InternalDigisosSoker()

        every { eventService.createModel(any(), any()) } returns model
        every { mockDigisosSak.ettersendtInfoNAV?.ettersendelser } returns emptyList()

        val list = service.hentAlleOpplastedeVedlegg(id, "token")

        assertThat(list).hasSize(2)
        assertThat(list[0].type).isEqualTo(dokumenttype)
        assertThat(list[0].innsendelsesfrist).isNull()
        assertThat(list[1].type).isEqualTo(dokumenttype_2)
        assertThat(list[1].innsendelsesfrist).isNull()
    }

    @Test
    fun `skal filtrere vekk vedlegg som ikke er LastetOpp`() {
        val model = InternalDigisosSoker()

        every { eventService.createModel(any(), any()) } returns model
        every { fiksClient.hentDokument(any(), vedleggMetadata_soknad_1, any(), any()) } returns mockJsonVedleggSpesifikasjon

        every { mockDigisosSak.ettersendtInfoNAV?.ettersendelser } returns listOf(
                Ettersendelse(
                        navEksternRefId = "ref 3",
                        vedleggMetadata = vedleggMetadata_ettersendelse_3,
                        vedlegg = listOf(DokumentInfo(ettersendelse_filnavn_1, dokumentlagerId_1, 42)),
                        timestampSendt = tid_1.toEpochMilli()))

        val list = service.hentAlleOpplastedeVedlegg(id, "token")

        assertThat(list).hasSize(0)
    }

    @Test
    fun `skal kun returne ettersendte vedlegg hvis soknaden ikke har noen vedlegg`() {
        val model = InternalDigisosSoker()

        every { eventService.createModel(any(), any()) } returns model
        every { fiksClient.hentDokument(any(), vedleggMetadata_soknad_1, any(), any()) } returns mockJsonVedleggSpesifikasjon

        val list = service.hentAlleOpplastedeVedlegg(id, "token")

        assertThat(list).hasSize(4)
        assertThat(list[0].type).isEqualTo(dokumenttype_3)
        assertThat(list[0].innsendelsesfrist).isNull()

        assertThat(list[1].type).isEqualTo(dokumenttype_4)
        assertThat(list[1].innsendelsesfrist).isNull()

        assertThat(list[2].type).isEqualTo(dokumenttype_3)
        assertThat(list[2].innsendelsesfrist).isNull()

        assertThat(list[3].type).isEqualTo(dokumenttype_3)
        assertThat(list[3].innsendelsesfrist).isNull()
    }

    @Test
    fun `skal hente alle vedlegg for digisosSak`() {
        val model = InternalDigisosSoker()

        every { eventService.createModel(any(), any()) } returns model

        val list = service.hentAlleOpplastedeVedlegg(id, "token")

        assertThat(list).hasSize(6)

        // nano-presisjon lacking
        assertThat(list[0].type).isEqualTo(dokumenttype)
        assertThat(list[0].datoLagtTil).isEqualToIgnoringNanos(LocalDateTime.ofInstant(tid_soknad, zoneIdOslo))

        assertThat(list[1].type).isEqualTo(dokumenttype_2)
        assertThat(list[1].datoLagtTil).isEqualToIgnoringNanos(LocalDateTime.ofInstant(tid_soknad, zoneIdOslo))

        assertThat(list[2].type).isEqualTo(dokumenttype_3)
        assertThat(list[2].datoLagtTil).isEqualToIgnoringNanos(LocalDateTime.ofInstant(tid_1, zoneIdOslo))

        assertThat(list[3].type).isEqualTo(dokumenttype_4)
        assertThat(list[3].datoLagtTil).isEqualToIgnoringNanos(LocalDateTime.ofInstant(tid_1, zoneIdOslo))

        assertThat(list[4].type).isEqualTo(dokumenttype_3)
        assertThat(list[4].datoLagtTil).isEqualToIgnoringNanos(LocalDateTime.ofInstant(tid_2, zoneIdOslo))

        assertThat(list[5].type).isEqualTo(dokumenttype_3)
        assertThat(list[5].datoLagtTil).isEqualToIgnoringNanos(LocalDateTime.ofInstant(tid_2, zoneIdOslo))
    }

    @Test
    fun `skal hente soknadsvedlegg filtrert pa status for digisosSak`() {
        val model = InternalDigisosSoker()

        every { eventService.createModel(any(), any()) } returns model
        val lastetOppList = service.hentSoknadVedleggMedStatus(LASTET_OPP_STATUS, id, originalSoknadMedVedleggKrevesOgLastetOpp, "token")
        val vedleggKrevesList = service.hentSoknadVedleggMedStatus(VEDLEGG_KREVES_STATUS, id, originalSoknadMedVedleggKrevesOgLastetOpp, "token")

        assertThat(lastetOppList).hasSize(1)
        assertThat(vedleggKrevesList).hasSize(1)

        // nano-presisjon lacking
        val zoneIdOslo = ZoneId.of("Europe/Oslo")
        assertThat(lastetOppList[0].type).isEqualTo(dokumenttype)
        assertThat(lastetOppList[0].datoLagtTil).isEqualToIgnoringNanos(LocalDateTime.ofInstant(tid_soknad, zoneIdOslo))

        assertThat(vedleggKrevesList[0].type).isEqualTo(dokumenttype_2)
        assertThat(vedleggKrevesList[0].datoLagtTil).isEqualToIgnoringNanos(LocalDateTime.ofInstant(tid_soknad, zoneIdOslo))
    }

    @Test
    fun `like filnavn i DokumentInfoList vil resultere i at de returneres for hver JsonFil med samme filnavn`() {
        val model = InternalDigisosSoker()

        every { eventService.createModel(any(), any()) } returns model
        every { fiksClient.hentDokument(any(), vedleggMetadata_soknad_1, any(), any()) } returns mockJsonVedleggSpesifikasjon
        every { fiksClient.hentDokument(any(), vedleggMetadata_ettersendelse_5, any(), any()) } returns
                JsonVedleggSpesifikasjon()
                        .withVedlegg(listOf(
                                JsonVedlegg()
                                        .withFiler(listOf(
                                                JsonFiler().withFilnavn(ettersendelse_filnavn_1).withSha512("1231231"),
                                                JsonFiler().withFilnavn(ettersendelse_filnavn_2).withSha512("adfgbjn")))
                                        .withStatus(LASTET_OPP_STATUS)
                                        .withType(dokumenttype_3),
                                JsonVedlegg()
                                        .withFiler(listOf(
                                                JsonFiler().withFilnavn(ettersendelse_filnavn_2).withSha512("aasdcx"),
                                                JsonFiler().withFilnavn(ettersendelse_filnavn_4).withSha512("qweqqa")))
                                        .withStatus(LASTET_OPP_STATUS)
                                        .withType(dokumenttype_4)
                        ))
        every { mockDigisosSak.ettersendtInfoNAV?.ettersendelser } returns listOf(
                Ettersendelse(
                        navEksternRefId = "ref 3",
                        vedleggMetadata = vedleggMetadata_ettersendelse_5,
                        vedlegg = listOf(
                                DokumentInfo(ettersendelse_filnavn_1, dokumentlagerId_1, 1),
                                DokumentInfo(ettersendelse_filnavn_2, dokumentlagerId_2, 2), // samme filnavn
                                DokumentInfo(ettersendelse_filnavn_2, dokumentlagerId_3, 3), // samme filnavn
                                DokumentInfo(ettersendelse_filnavn_4, dokumentlagerId_4, 4)),
                        timestampSendt = tid_1.toEpochMilli()))

        val list = service.hentAlleOpplastedeVedlegg(id, "token")

        assertThat(list).hasSize(2)
        assertThat(list[0].innsendelsesfrist).isNull()
        assertThat(list[1].innsendelsesfrist).isNull()
    }

    @Test
    fun `skal knytte innsendelsesfrist fra oppgave til vedlegg`() {
        val frist = LocalDateTime.ofInstant(tid_soknad, zoneIdOslo).plusDays(14)
        val frist2 = LocalDateTime.ofInstant(tid_soknad, zoneIdOslo).plusDays(21)
        val datoLagtTil = LocalDateTime.ofInstant(tid_soknad, zoneIdOslo).plusDays(2)
        val model = InternalDigisosSoker()
        model.oppgaver.add(Oppgave(dokumenttype_3, null, frist, datoLagtTil, true))
        model.oppgaver.add(Oppgave(dokumenttype_4, null, frist2, datoLagtTil, true))

        every { eventService.createModel(any(), any()) } returns model

        val list = service.hentAlleOpplastedeVedlegg(id, "token")

        assertThat(list).hasSize(6)

        assertThat(list[0].type).isEqualTo(dokumenttype)
        assertThat(list[0].innsendelsesfrist).isNull()

        assertThat(list[1].type).isEqualTo(dokumenttype_2)
        assertThat(list[1].innsendelsesfrist).isNull()

        assertThat(list[2].type).isEqualTo(dokumenttype_3)
        assertThat(list[2].innsendelsesfrist).isEqualTo(frist)

        assertThat(list[3].type).isEqualTo(dokumenttype_4)
        assertThat(list[3].innsendelsesfrist).isEqualTo(frist2)

        assertThat(list[4].type).isEqualTo(dokumenttype_3)
        assertThat(list[4].innsendelsesfrist).isEqualTo(frist)

        assertThat(list[5].type).isEqualTo(dokumenttype_3)
        assertThat(list[5].innsendelsesfrist).isEqualTo(frist)
    }

    @Test
    internal fun `utestaaende oppgaver skal mappes som manglende vedlegg`() {
        val frist = LocalDateTime.ofInstant(tid_soknad, zoneIdOslo).plusDays(14)
        val frist2 = LocalDateTime.ofInstant(tid_soknad, zoneIdOslo).plusDays(21)
        val datoLagtTil = LocalDateTime.ofInstant(tid_soknad, zoneIdOslo).plusDays(2)
        val model = InternalDigisosSoker()
        model.oppgaver.add(Oppgave(dokumenttype_3, null, frist, datoLagtTil, true))
        model.oppgaver.add(Oppgave(dokumenttype, null, frist2, datoLagtTil, true))

        every { eventService.createModel(any(), any()) } returns model
        every { mockDigisosSak.ettersendtInfoNAV?.ettersendelser } returns listOf(
                Ettersendelse(
                        navEksternRefId = "ref 1",
                        vedleggMetadata = vedleggMetadata_ettersendelse_1,
                        vedlegg = listOf(DokumentInfo(ettersendelse_filnavn_1, dokumentlagerId_1, 42), DokumentInfo(ettersendelse_filnavn_2, dokumentlagerId_2, 42)),
                        timestampSendt = tid_1.toEpochMilli()))
        every { fiksClient.hentDokument(any(), vedleggMetadata_soknad_1, any(), any()) } returns mockJsonVedleggSpesifikasjon

        val list = service.hentAlleOpplastedeVedlegg(id, "token")

        assertThat(list).hasSize(3)

        // Ettersendt vedlegg matches med oppgave
        assertThat(list[0].type).isEqualTo(dokumenttype_3)
        assertThat(list[0].tilleggsinfo).isNull()
        assertThat(list[0].innsendelsesfrist).isEqualTo(frist)
        assertThat(list[0].antallFiler).isEqualTo(1)
        assertThat(list[0].datoLagtTil).isEqualToIgnoringNanos(LocalDateTime.ofInstant(tid_1, zoneIdOslo))

        // Ettersendt vedlegg matches ikke med oppgave
        assertThat(list[1].type).isEqualTo(dokumenttype_4)
        assertThat(list[1].tilleggsinfo).isNull()
        assertThat(list[1].innsendelsesfrist).isNull()
        assertThat(list[1].antallFiler).isEqualTo(1)
        assertThat(list[1].datoLagtTil).isEqualToIgnoringNanos(LocalDateTime.ofInstant(tid_1, zoneIdOslo))

        // Utestående oppgave legges til
        assertThat(list[2].type).isEqualTo(dokumenttype)
        assertThat(list[2].tilleggsinfo).isNull()
        assertThat(list[2].innsendelsesfrist).isEqualTo(frist2)
        assertThat(list[2].antallFiler).isEqualTo(0)
        assertThat(list[2].datoLagtTil).isNull()
    }
}

private const val id = "123"

private const val ettersendelse_filnavn_1 = "filnavn.pdf"
private const val ettersendelse_filnavn_2 = "navn på fil.ocr"
private const val ettersendelse_filnavn_3 = "denne filens navn.jpg"
private const val ettersendelse_filnavn_4 = "gif.jpg"
private const val soknad_filnavn_1 = "originalSoknadVedlegg.png"
private const val soknad_filnavn_2 = "originalSoknadVedlegg_2.exe"

private const val dokumentlagerId_1 = "9999"
private const val dokumentlagerId_2 = "7777"
private const val dokumentlagerId_3 = "5555"
private const val dokumentlagerId_4 = "3333"
private const val dokumentlagerId_soknad_1 = "1111"
private const val dokumentlagerId_soknad_2 = "1234"

private const val dokumenttype = "type"
private const val dokumenttype_2 = "type 2"
private const val dokumenttype_3 = "type 3"
private const val dokumenttype_4 = "type 4"

private val tid_1 = Instant.now()
private val tid_2 = Instant.now().minus(2, ChronoUnit.DAYS)
private val tid_soknad = Instant.now().minus(14, ChronoUnit.DAYS)

private const val vedleggMetadata_ettersendelse_1 = "vedlegg metadata 1"
private const val vedleggMetadata_ettersendelse_2 = "vedlegg metadata 2"
private const val vedleggMetadata_ettersendelse_3 = "vedlegg metadata 3"
private const val vedleggMetadata_ettersendelse_4 = "vedlegg metadata 4"
private const val vedleggMetadata_ettersendelse_5 = "vedlegg metadata 5"
private const val vedleggMetadata_soknad_1 = "vedlegg metadata soknad"
private const val vedleggMetadata_soknad_2 = "vedlegg metadata soknad med vedlegg kreves og lastet opp"

private val ettersendelser = listOf(
        Ettersendelse(
                navEksternRefId = "ref 1",
                vedleggMetadata = vedleggMetadata_ettersendelse_1,
                vedlegg = listOf(DokumentInfo(ettersendelse_filnavn_1, dokumentlagerId_1, 42), DokumentInfo(ettersendelse_filnavn_2, dokumentlagerId_2, 42)),
                timestampSendt = tid_1.toEpochMilli()),
        Ettersendelse(
                navEksternRefId = "ref 2",
                vedleggMetadata = vedleggMetadata_ettersendelse_2,
                vedlegg = listOf(DokumentInfo(ettersendelse_filnavn_3, dokumentlagerId_3, 42), DokumentInfo(ettersendelse_filnavn_4, dokumentlagerId_4, 84)),
                timestampSendt = tid_2.toEpochMilli()),
        Ettersendelse(
                navEksternRefId = "ref 2",
                vedleggMetadata = vedleggMetadata_ettersendelse_4,
                vedlegg = listOf(DokumentInfo(ettersendelse_filnavn_4, dokumentlagerId_3, 1), DokumentInfo(ettersendelse_filnavn_4, dokumentlagerId_4, 2)),
                timestampSendt = tid_2.toEpochMilli())
)

private val originalSoknad = OriginalSoknadNAV(
        navEksternRefId = "123",
        metadata = "metadata",
        vedleggMetadata = vedleggMetadata_soknad_1,
        soknadDokument = mockk(),
        vedlegg = listOf(DokumentInfo(soknad_filnavn_1, dokumentlagerId_soknad_1, 1337), DokumentInfo(soknad_filnavn_2, dokumentlagerId_soknad_2, 1337)),
        timestampSendt = tid_soknad.toEpochMilli()
)

private val originalSoknadMedVedleggKrevesOgLastetOpp = OriginalSoknadNAV(
        navEksternRefId = "123",
        metadata = "metadata",
        vedleggMetadata = vedleggMetadata_soknad_2,
        soknadDokument = mockk(),
        vedlegg = listOf(DokumentInfo(soknad_filnavn_1, dokumentlagerId_soknad_1, 1337)),
        timestampSendt = tid_soknad.toEpochMilli()
)

private val soknadVedleggSpesifikasjon = JsonVedleggSpesifikasjon()
        .withVedlegg(listOf(
                JsonVedlegg()
                        .withFiler(listOf(
                                JsonFiler().withFilnavn(soknad_filnavn_1).withSha512("1234fasd")))
                        .withStatus(LASTET_OPP_STATUS)
                        .withType(dokumenttype),
                JsonVedlegg()
                        .withFiler(listOf(
                                JsonFiler().withFilnavn(soknad_filnavn_2).withSha512("sfg234")))
                        .withStatus(LASTET_OPP_STATUS)
                        .withType(dokumenttype_2)
        ))

private val soknadVedleggSpesifikasjonMedStatusKrevesOgLastetOpp = JsonVedleggSpesifikasjon()
        .withVedlegg(listOf(
                JsonVedlegg()
                        .withFiler(listOf(
                                JsonFiler().withFilnavn(soknad_filnavn_1).withSha512("1234fasd")))
                        .withStatus(LASTET_OPP_STATUS)
                        .withType(dokumenttype),
                JsonVedlegg()
                        .withFiler(listOf())
                        .withStatus("VedleggKreves")
                        .withType(dokumenttype_2)
        ))

private val ettersendteVedleggSpesifikasjon_1 = JsonVedleggSpesifikasjon()
        .withVedlegg(listOf(
                JsonVedlegg()
                        .withFiler(listOf(
                                JsonFiler().withFilnavn(ettersendelse_filnavn_1).withSha512("g25b3")))
                        .withStatus(LASTET_OPP_STATUS)
                        .withType(dokumenttype_3),
                JsonVedlegg()
                        .withFiler(listOf(
                                JsonFiler().withFilnavn(ettersendelse_filnavn_2).withSha512("4avc65a8")))
                        .withStatus(LASTET_OPP_STATUS)
                        .withType(dokumenttype_4)
        ))

private val ettersendteVedleggSpesifikasjon_2 = JsonVedleggSpesifikasjon()
        .withVedlegg(listOf(
                JsonVedlegg()
                        .withFiler(listOf(
                                JsonFiler().withFilnavn(ettersendelse_filnavn_3).withSha512("aadsfwr"),
                                JsonFiler().withFilnavn(ettersendelse_filnavn_4).withSha512("uiuusss")))
                        .withStatus(LASTET_OPP_STATUS)
                        .withType(dokumenttype_3)
        ))

private val ettersendteVedleggSpesifikasjon_3 = JsonVedleggSpesifikasjon()
        .withVedlegg(listOf(
                JsonVedlegg()
                        .withFiler(listOf(
                                JsonFiler().withFilnavn(ettersendelse_filnavn_3).withSha512("aadsfwr")))
                        .withStatus("VedleggAlleredeSendt")
                        .withType(dokumenttype_3)
        ))

private val ettersendteVedleggSpesifikasjon_4 = JsonVedleggSpesifikasjon()
        .withVedlegg(listOf(
                JsonVedlegg()
                        .withFiler(listOf(
                                JsonFiler().withFilnavn(ettersendelse_filnavn_4).withSha512("1231231")))
                        .withStatus(LASTET_OPP_STATUS)
                        .withType(dokumenttype_3)
        ))