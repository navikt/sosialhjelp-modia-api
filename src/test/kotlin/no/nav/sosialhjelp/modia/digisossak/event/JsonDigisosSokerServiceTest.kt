package no.nav.sosialhjelp.modia.digisossak.event

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class JsonDigisosSokerServiceTest {
    private val fiksClient: FiksClient = mockk()
    private val jsonDigisosSokerService = JsonDigisosSokerService(fiksClient)

    @BeforeEach
    fun init() {
        clearMocks(fiksClient)
    }

    @Test
    suspend fun `Should gather innsyn data`() {
        val mockJsonDigisosSoker: JsonDigisosSoker = mockk()

        coEvery { fiksClient.hentDokument(any(), any(), any(), JsonDigisosSoker::class.java, "abc_123") } returns mockJsonDigisosSoker

        val jsonDigisosSoker: JsonDigisosSoker? = jsonDigisosSokerService.get("fnr", "123", "abc", 123L)

        assertThat(jsonDigisosSoker).isNotNull
    }

    @Test
    suspend fun `Should return null if DigisosSoker is null`() {
        val jsonDigisosSoker = jsonDigisosSokerService.get("fnr", "123", null, 123L)

        assertThat(jsonDigisosSoker).isNull()
    }

    @Test
    suspend fun `Should use metadata and timestamp as cache key`() {
        val slot = slot<String>()
        coEvery { fiksClient.hentDokument(any(), any(), any(), JsonDigisosSoker::class.java, capture(slot)) } returns mockk()

        jsonDigisosSokerService.get("fnr", "123", "abc", 123L)

        assertThat(slot.captured).isEqualTo("abc_123")
    }
}
