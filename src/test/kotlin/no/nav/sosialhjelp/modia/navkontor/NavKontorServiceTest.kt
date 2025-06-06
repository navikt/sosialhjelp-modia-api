package no.nav.sosialhjelp.modia.navkontor

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.sosialhjelp.modia.navkontor.norg.NavEnhet
import no.nav.sosialhjelp.modia.navkontor.norg.NorgClient
import no.nav.sosialhjelp.modia.redis.RedisService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class NavKontorServiceTest {
    private val norgUrl = "norgUrl"
    private val norgClient: NorgClient = mockk()
    private val redisService: RedisService = mockk()

    private val navKontorService = NavKontorService(norgUrl, norgClient, redisService)

    private val enhetsnr = "1234"
    private val annetEnhetsnr = "2222"

    private val navEnhet = NavEnhet(1, "Nav test", enhetsnr, "AKTIV", 1, "", "", "sosiale tjenester", "LOKAL")
    private val annenNavEnhet = NavEnhet(2, "Nav 2", annetEnhetsnr, "AKTIV", 1, "", "", "sosiale tjenester", "LOKAL")
    private val navEnhetUtenSosialTjenesterInformasjon = NavEnhet(3, "Nav 3", "annet_enhetsnr", "AKTIV", 1, "", "", "", "LOKAL")
    private val navEnhetIkkeLokal = NavEnhet(4, "Nav 4", "annet_enhetsnr", "AKTIV", 1, "", "", "", "IKKE_LOKAL")

    @Test
    internal fun skalHenteNavEnhet() {
        every { norgClient.hentNavEnhet(enhetsnr) } returns navEnhet

        val navKontorinfo = navKontorService.hentNavKontorinfo(enhetsnr)

        assertThat(navKontorinfo).isNotNull
        assertThat(navKontorinfo?.enhetsnr).isEqualTo(enhetsnr)
        verify(exactly = 1) { norgClient.hentNavEnhet(enhetsnr) }
    }

    @Test
    internal fun `skal returnere null hvis sosialTjenester er null eller tom`() {
        every { norgClient.hentNavEnhet(enhetsnr) } returns navEnhetUtenSosialTjenesterInformasjon

        val navKontorinfo = navKontorService.hentNavKontorinfo(enhetsnr)

        assertThat(navKontorinfo).isNull()
    }

    @Test
    internal fun `skal returnere tom liste hvis cache er tom og norgClient gir tom liste`() {
        every { redisService.getAlleNavEnheter() } returns null
        every { norgClient.hentAlleNavEnheter() } returns emptyList()

        val response = navKontorService.hentAlleNavKontorinfo()

        assertThat(response).isEmpty()
    }

    @Test
    internal fun `skal hente alleNavEnheter fra cache`() {
        every { redisService.getAlleNavEnheter() } returns listOf(annenNavEnhet, navEnhet)

        val response = navKontorService.hentAlleNavKontorinfo()

        assertThat(response).isNotEmpty
        assertThat(response).hasSize(2)

        verify(exactly = 0) { norgClient.hentAlleNavEnheter() }
    }

    @Test
    internal fun `skal filtrere vekk navEnheter med type ulik LOKAL`() {
        every { redisService.getAlleNavEnheter() } returns null
        every { norgClient.hentAlleNavEnheter() } returns listOf(navEnhet, navEnhetIkkeLokal)

        val response = navKontorService.hentAlleNavKontorinfo()

        assertThat(response).isNotEmpty
        assertThat(response).hasSize(1)
        assertThat(response[0].enhetsnr).isEqualTo(enhetsnr)
    }
}
