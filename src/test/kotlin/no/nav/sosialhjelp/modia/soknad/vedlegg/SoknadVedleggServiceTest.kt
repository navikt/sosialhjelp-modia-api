package no.nav.sosialhjelp.modia.soknad.vedlegg

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.DokumentInfo
import no.nav.sosialhjelp.api.fiks.OriginalSoknadNAV
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

internal class SoknadVedleggServiceTest {

    private val fiksClient: FiksClient = mockk()
    private val service = SoknadVedleggService(fiksClient)

    private val mockDigisosSak: DigisosSak = mockk()
    private val mockJsonVedleggSpesifikasjon: JsonVedleggSpesifikasjon = mockk()

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { fiksClient.hentDigisosSak(any()) } returns mockDigisosSak
        every { mockDigisosSak.fiksDigisosId } returns "id"
        every { mockDigisosSak.sokerFnr } returns "fnr"

        every { mockJsonVedleggSpesifikasjon.vedlegg } returns emptyList()

        every { fiksClient.hentDokument<JsonVedleggSpesifikasjon>(any(), any(), vedleggMetadata_soknad_2, any()) } returns soknadVedleggSpesifikasjonMedStatusKrevesOgLastetOpp
    }

    @Test
    fun `skal hente soknadsvedlegg filtrert pa status for digisosSak`() {
        every { mockDigisosSak.originalSoknadNAV } returns originalSoknadMedVedleggKrevesOgLastetOpp

        val lastetOppList = service.hentSoknadVedleggMedStatus(mockDigisosSak, LASTET_OPP_STATUS)
        val vedleggKrevesList = service.hentSoknadVedleggMedStatus(mockDigisosSak, VEDLEGG_KREVES_STATUS)

        assertThat(lastetOppList).hasSize(1)
        assertThat(vedleggKrevesList).hasSize(1)

        // nano-presisjon lacking
        val zoneIdOslo = ZoneId.of("Europe/Oslo")
        assertThat(lastetOppList[0].type).isEqualTo(dokumenttype)
        assertThat(lastetOppList[0].datoLagtTil).isEqualToIgnoringNanos(LocalDateTime.ofInstant(tid_soknad, zoneIdOslo))

        assertThat(vedleggKrevesList[0].type).isEqualTo(dokumenttype_2)
        assertThat(vedleggKrevesList[0].datoLagtTil).isEqualToIgnoringNanos(LocalDateTime.ofInstant(tid_soknad, zoneIdOslo))
    }

    companion object {
        private const val soknad_filnavn_1 = "originalSoknadVedlegg.png"

        private const val dokumentlagerId_soknad_1 = "1111"

        private const val dokumenttype = "type"
        private const val dokumenttype_2 = "type 2"

        private val tid_soknad = Instant.now().minus(14, ChronoUnit.DAYS)

        private const val vedleggMetadata_soknad_1 = "vedlegg metadata soknad"
        private const val vedleggMetadata_soknad_2 = "vedlegg metadata soknad med vedlegg kreves og lastet opp"

        private val originalSoknadMedVedleggKrevesOgLastetOpp = OriginalSoknadNAV(
            navEksternRefId = "123",
            metadata = "metadata",
            vedleggMetadata = vedleggMetadata_soknad_2,
            soknadDokument = mockk(),
            vedlegg = listOf(DokumentInfo(soknad_filnavn_1, dokumentlagerId_soknad_1, 1337)),
            timestampSendt = tid_soknad.toEpochMilli()
        )

        private val soknadVedleggSpesifikasjonMedStatusKrevesOgLastetOpp = JsonVedleggSpesifikasjon()
            .withVedlegg(
                listOf(
                    JsonVedlegg()
                        .withFiler(
                            listOf(
                                JsonFiler().withFilnavn(soknad_filnavn_1).withSha512("1234fasd")
                            )
                        )
                        .withStatus(LASTET_OPP_STATUS)
                        .withType(dokumenttype),
                    JsonVedlegg()
                        .withFiler(listOf())
                        .withStatus("VedleggKreves")
                        .withType(dokumenttype_2)
                )
            )
    }
}
