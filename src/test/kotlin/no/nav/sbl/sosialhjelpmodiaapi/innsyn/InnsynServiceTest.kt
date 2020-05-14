package no.nav.sbl.sosialhjelpmodiaapi.innsyn

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.service.innsyn.InnsynService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class InnsynServiceTest {

    private val fiksClient: FiksClient = mockk()
    private val service = InnsynService(fiksClient)

    @BeforeEach
    fun init() {
        clearMocks(fiksClient)
    }

    @Test
    fun `Should gather innsyn data`() {
        val mockJsonDigisosSoker: JsonDigisosSoker = mockk()

        every { fiksClient.hentDokument(any(), any(), JsonDigisosSoker::class.java, "token") } returns mockJsonDigisosSoker

        val jsonDigisosSoker: JsonDigisosSoker? = service.hentJsonDigisosSoker("123", "abc", "token")

        assertThat(jsonDigisosSoker).isNotNull
    }

    @Test
    fun `Should return null if DigisosSoker is null`() {
        val jsonDigisosSoker = service.hentJsonDigisosSoker("123", null, "token")

        assertThat(jsonDigisosSoker).isNull()
    }

    @Test
    fun `Should return originalSoknad`() {
        val mockJsonSoknad: JsonSoknad = mockk()
        every { fiksClient.hentDokument(any(), any(), JsonSoknad::class.java, "token") } returns mockJsonSoknad

        val jsonSoknad: JsonSoknad? = service.hentOriginalSoknad("123", "abc", "token")

        assertThat(jsonSoknad).isNotNull
    }

    @Test
    fun `Should return null if originalSoknadNAV is null`() {
        val jsonSoknad: JsonSoknad? = service.hentOriginalSoknad("123", null, "token")

        assertThat(jsonSoknad).isNull()
    }
}