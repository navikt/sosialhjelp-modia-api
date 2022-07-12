package no.nav.sosialhjelp.modia.kommune

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.sosialhjelp.modia.kommune.kartverket.ContainedItem
import no.nav.sosialhjelp.modia.kommune.kartverket.KommunenavnClient
import no.nav.sosialhjelp.modia.kommune.kartverket.KommunenavnProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KommunenavnServiceTest {

    private val kommunenavnProperties: KommunenavnProperties = mockk()

    private val kommunenavnClient: KommunenavnClient = mockk()

    private val service = KommunenavnService(kommunenavnClient)

    private val kommuneNr = "1234"
    private val kommunenavn = "Oslo"
    private val osloContainedItem = ContainedItem(
        "",
        "",
        kommuneNr,
        "",
        "",
        kommunenavn,
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        1
    )

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `Skal returnere kommunenavn nar kommunenummer er kjent`() {
        val containedItems: List<ContainedItem> = listOf(osloContainedItem)
        every { kommunenavnProperties.containeditems } returns containedItems
        every { kommunenavnClient.getAll() } returns kommunenavnProperties

        val kommunenavn = service.hentKommunenavnFor(kommuneNr)

        assertThat(kommunenavn).isEqualTo(kommunenavn)
    }

    @Test
    fun `Skal returnere feiltekst nar kommunenummer er ukjent`() {
        every { kommunenavnClient.getAll() } returns kommunenavnProperties
        val containedItems: List<ContainedItem> = ArrayList()
        every { kommunenavnProperties.containeditems } returns containedItems

        val kommunenavn = service.hentKommunenavnFor(kommuneNr)

        assertThat(kommunenavn).isEqualTo("[Kan ikke hente kommune for kommunenummer \"$kommuneNr\"]")
    }

    @Test
    fun `Skal brukce cache pa andre oppslag`() {
        val containedItems: List<ContainedItem> = listOf(osloContainedItem)
        every { kommunenavnProperties.containeditems } returns containedItems
        every { kommunenavnClient.getAll() } returns kommunenavnProperties

        val kommunenavn1 = service.hentKommunenavnFor(kommuneNr)
        assertThat(kommunenavn1).isEqualTo(kommunenavn)

        val kommunenavn2 = service.hentKommunenavnFor(kommuneNr)
        assertThat(kommunenavn2).isEqualTo(kommunenavn)
        verify(exactly = 1) {
            kommunenavnClient.getAll()
        }
    }
}
