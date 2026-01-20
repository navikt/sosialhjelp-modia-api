package no.nav.sosialhjelp.modia.kommune

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.sosialhjelp.modia.kommune.kartverket.KommunenavnClient
import no.nav.sosialhjelp.modia.kommune.kartverket.KommunenavnProperties
import no.nav.sosialhjelp.modia.utils.sosialhjelpJsonMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.readValue

internal class KommunenavnServiceTest {
    private val kommunenavnClient: KommunenavnClient = mockk()
    private val service = KommunenavnService(kommunenavnClient)

    private val kommuneNr = "0301"
    private val kommunenavn = "Oslo"

    private val response =
        sosialhjelpJsonMapper.readValue<KommunenavnProperties>(
            ClassLoader.getSystemResourceAsStream("kartverket-response.json")!!,
        )

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `Skal returnere kommunenavn nar kommunenummer er kjent`() {
        every { kommunenavnClient.getAll() } returns response

        val kommunenavn = service.hentKommunenavnFor(kommuneNr)

        assertThat(kommunenavn).isEqualTo(kommunenavn)
    }

    @Test
    fun `Skal returnere feiltekst nar kommunenummer er ukjent`() {
        every { kommunenavnClient.getAll() } returns response.copy(containeditems = listOf())

        val kommunenavn = service.hentKommunenavnFor(kommuneNr)

        assertThat(kommunenavn).isEqualTo("[Kan ikke hente kommune for kommunenummer \"$kommuneNr\"]")
    }

    @Test
    fun `Skal brukce cache pa andre oppslag`() {
        every { kommunenavnClient.getAll() } returns response

        val kommunenavn1 = service.hentKommunenavnFor(kommuneNr)
        assertThat(kommunenavn1).isEqualTo(kommunenavn)

        val kommunenavn2 = service.hentKommunenavnFor(kommuneNr)
        assertThat(kommunenavn2).isEqualTo(kommunenavn)
        verify(exactly = 1) {
            kommunenavnClient.getAll()
        }
    }
}
