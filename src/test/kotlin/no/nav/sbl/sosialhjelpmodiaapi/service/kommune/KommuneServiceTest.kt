package no.nav.sbl.sosialhjelpmodiaapi.service.kommune

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.redis.RedisService
import no.nav.sbl.sosialhjelpmodiaapi.responses.ok_kommuneinfo_response_string
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_IKKE_MULIG
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SKAL_VISE_FEILSIDE
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SOM_VANLIG
import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KommuneServiceTest {

    private val fiksClient: FiksClient = mockk()
    private val kommuneInfoClient: KommuneInfoClient = mockk()
    private val redisService: RedisService = mockk()

    private val service = KommuneService(fiksClient, kommuneInfoClient, redisService)

    private val mockDigisosSak: DigisosSak = mockk()
    private val kommuneNr = "1234"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { fiksClient.hentDigisosSak(any()) } returns mockDigisosSak
        every { mockDigisosSak.kommunenummer } returns kommuneNr

        every { redisService.get(any(), any()) } returns null
        every { redisService.put(any(), any()) } just Runs
    }

    @Test
    fun `Kommune har konfigurasjon men skal sende via svarut`() {
        every { kommuneInfoClient.get(any()) } returns KommuneInfo(kommuneNr, false, false, false, false, null, true, null)

        val status = service.getStatus("123")

        assertThat(status).isEqualTo(HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)
    }

    @Test
    fun `Kommune skal sende soknader og ettersendelser via FIKS API`() {
        every { kommuneInfoClient.get(any()) } returns KommuneInfo(kommuneNr, true, false, false, false, null, true, null)

        val status1 = service.getStatus("123")

        assertThat(status1).isEqualTo(SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA)

        every { kommuneInfoClient.get(any()) } returns KommuneInfo(kommuneNr, true, true, false, false, null, true, null)

        val status2 = service.getStatus("123")

        assertThat(status2).isEqualTo(SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA)
    }

    @Test
    fun `Kommune skal vise midlertidig feilside og innsyn som vanlig`() {
        every { kommuneInfoClient.get(any()) } returns KommuneInfo(kommuneNr, true, true, true, false, null, true, null)

        val status = service.getStatus("123")

        assertThat(status).isEqualTo(SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SOM_VANLIG)
    }

    @Test
    fun `Kommune skal vise midlertidig feilside og innsyn er ikke mulig`() {
        every { kommuneInfoClient.get(any()) } returns KommuneInfo(kommuneNr, true, false, true, false, null, true, null)

        val status = service.getStatus("123")

        assertThat(status).isEqualTo(SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_IKKE_MULIG)
    }

    @Test
    fun `Kommune skal vise midlertidig feilside og innsyn skal vise feilside`() {
        every { kommuneInfoClient.get(any()) } returns KommuneInfo(kommuneNr, true, true, true, true, null, true, null)

        val status = service.getStatus("123")

        assertThat(status).isEqualTo(SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SKAL_VISE_FEILSIDE)
    }

    @Test
    fun `Ingen originalSoknad - skal kaste feil`() {
        every { mockDigisosSak.kommunenummer } returns ""

        assertThatExceptionOfType(RuntimeException::class.java).isThrownBy { service.getStatus("123") }
                .withMessage("KommuneStatus kan ikke hentes uten kommunenummer")
    }

    @Test
    internal fun `hent KommuneInfo fra cache`() {
        val kommuneInfo: KommuneInfo = objectMapper.readValue(ok_kommuneinfo_response_string)
        every { redisService.get(any(), any()) } returns kommuneInfo

        val result = service.get(kommuneNr)

        assertThat(result).isNotNull
        verify(exactly = 1) { redisService.get(any(), any()) }
        verify(exactly = 0) { kommuneInfoClient.get(any()) }
        verify(exactly = 0) { redisService.put(any(), any()) }
    }

    @Test
    internal fun `hent KommuneInfo fra client`() {
        every { kommuneInfoClient.get(any()) } returns objectMapper.readValue(ok_kommuneinfo_response_string)

        val result = service.get(kommuneNr)

        assertThat(result).isNotNull
        verify(exactly = 1) { redisService.get(any(), any()) }
        verify(exactly = 1) { kommuneInfoClient.get(any()) }
        verify(exactly = 1) { redisService.put(any(), any()) }
    }
}