package no.nav.sbl.sosialhjelpmodiaapi.service.kommune

import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.service.innsyn.InnsynService
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_IKKE_MULIG
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SKAL_VISE_FEILSIDE
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SOM_VANLIG
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KommuneServiceTest {

    private val fiksClient: FiksClient = mockk()

    private val service = KommuneService(fiksClient)

    private val mockDigisosSak: DigisosSak = mockk()
    private val kommuneNr = "1234"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { fiksClient.hentDigisosSak(any()) } returns mockDigisosSak
        every { mockDigisosSak.kommunenummer } returns kommuneNr
    }

    @Test
    fun `Kommune har konfigurasjon men skal sende via svarut`() {
        every { fiksClient.hentKommuneInfo(any()) } returns KommuneInfo(kommuneNr, false, false, false, false, null, true, null)

        val status = service.hentKommuneStatus("123")

        assertThat(status).isEqualTo(HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)
    }

    @Test
    fun `Kommune skal sende soknader og ettersendelser via FIKS API`() {
        every { fiksClient.hentKommuneInfo(any()) } returns KommuneInfo(kommuneNr, true, false, false, false, null, true, null)

        val status1 = service.hentKommuneStatus("123")

        assertThat(status1).isEqualTo(SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA)

        every { fiksClient.hentKommuneInfo(any()) } returns KommuneInfo(kommuneNr, true, true, false, false, null, true, null)

        val status2 = service.hentKommuneStatus("123")

        assertThat(status2).isEqualTo(SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA)
    }

    @Test
    fun `Kommune skal vise midlertidig feilside og innsyn som vanlig`() {
        every { fiksClient.hentKommuneInfo(any()) } returns KommuneInfo(kommuneNr, true, true, true, false, null, true, null)

        val status = service.hentKommuneStatus("123")

        assertThat(status).isEqualTo(SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SOM_VANLIG)
    }

    @Test
    fun `Kommune skal vise midlertidig feilside og innsyn er ikke mulig`() {
        every { fiksClient.hentKommuneInfo(any()) } returns KommuneInfo(kommuneNr, true, false, true, false, null, true, null)

        val status = service.hentKommuneStatus("123")

        assertThat(status).isEqualTo(SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_IKKE_MULIG)
    }

    @Test
    fun `Kommune skal vise midlertidig feilside og innsyn skal vise feilside`() {
        every { fiksClient.hentKommuneInfo(any()) } returns KommuneInfo(kommuneNr, true, true, true, true, null, true, null)

        val status = service.hentKommuneStatus("123")

        assertThat(status).isEqualTo(SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SKAL_VISE_FEILSIDE)
    }

    @Test
    fun `Ingen originalSoknad - skal kaste feil`() {
        every { mockDigisosSak.kommunenummer } returns ""

        assertThatExceptionOfType(RuntimeException::class.java).isThrownBy { service.hentKommuneStatus("123") }
                .withMessage("KommuneStatus kan ikke hentes uten kommunenummer")
    }
}