package no.nav.sosialhjelp.modia.digisossak.event

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
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
    fun `Should gather innsyn data`() {
        val mockJsonDigisosSoker: JsonDigisosSoker = mockk()

        every { fiksClient.hentDokument(any(), any(), any(), JsonDigisosSoker::class.java) } returns mockJsonDigisosSoker

        val jsonDigisosSoker: JsonDigisosSoker? = jsonDigisosSokerService.get("fnr", "123", "abc")

        assertThat(jsonDigisosSoker).isNotNull
    }

    @Test
    fun `Should return null if DigisosSoker is null`() {
        val jsonDigisosSoker = jsonDigisosSokerService.get("fnr", "123", null)

        assertThat(jsonDigisosSoker).isNull()
    }
}
