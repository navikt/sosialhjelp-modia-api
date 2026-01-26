package no.nav.sosialhjelp.modia.tilgang

import io.mockk.called
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sosialhjelp.modia.app.exceptions.ManglendeTilgangException
import no.nav.sosialhjelp.modia.logging.AuditService
import no.nav.sosialhjelp.modia.person.pdl.Adressebeskyttelse
import no.nav.sosialhjelp.modia.person.pdl.Gradering
import no.nav.sosialhjelp.modia.person.pdl.PdlClient
import no.nav.sosialhjelp.modia.person.pdl.PdlHentPerson
import no.nav.sosialhjelp.modia.person.pdl.PdlPerson
import no.nav.sosialhjelp.modia.tilgang.skjermedepersoner.SkjermedePersonerClient
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.BEARER
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod

internal class TilgangskontrollServiceTest {
    private val pdlClient: PdlClient = mockk()
    private val skjermedePersonerClient: SkjermedePersonerClient = mockk()
    private val auditService: AuditService = mockk()

    private val service = TilgangskontrollService(pdlClient, skjermedePersonerClient, auditService)

    private val fnr = "fnr"
    private val pdlPersonUtenBeskyttelse = PdlHentPerson(PdlPerson(emptyList(), emptyList(), emptyList(), emptyList(), emptyList()))

    @BeforeEach
    internal fun setUp() {
        every { pdlClient.hentPerson(fnr, any()) } returns pdlPersonUtenBeskyttelse
        every { skjermedePersonerClient.erPersonSkjermet(fnr, any()) } returns false
        every { auditService.reportToAuditlog(any(), any(), any(), any()) } just runs
    }

    @Test
    internal fun `harTilgang - kaster ingen exception`() {
        service.harTilgang(fnr, "token", "https://url.no/", HttpMethod.POST)
        verify { auditService wasNot called }
    }

    @Test
    internal fun `skal strippe bearer-prefiks fra token`() {
        val tokenString = "part1.part2.part3"
        val skjermToken = slot<String>()

        every { skjermedePersonerClient.erPersonSkjermet(fnr, capture(skjermToken)) } returns false

        service.harTilgang(fnr, "$BEARER$tokenString", "https://url.no/", HttpMethod.POST)

        assertThat(skjermToken.isCaptured).isTrue
        assertThat(skjermToken.captured).isEqualTo(tokenString)
        assertThat(skjermToken.captured).doesNotContain(BEARER)
    }

    @Test
    internal fun `manglende tilgang til kode6`() {
        val kode6 = listOf(Adressebeskyttelse(Gradering.STRENGT_FORTROLIG))
        val personMedKode6 = PdlHentPerson(PdlPerson(kode6, emptyList(), emptyList(), emptyList(), emptyList()))
        every { pdlClient.hentPerson(any(), any()) } returns personMedKode6

        assertThatThrownBy { service.harTilgang(fnr, "token", "https://url.no/", HttpMethod.POST) }
            .isInstanceOf(ManglendeTilgangException::class.java)
            .hasMessage("Person har addressebeskyttelse.")
    }

    @Test
    internal fun `manglende tilgang til kode6 utland`() {
        val kode6 = listOf(Adressebeskyttelse(Gradering.STRENGT_FORTROLIG_UTLAND))
        val personMedKode6 = PdlHentPerson(PdlPerson(kode6, emptyList(), emptyList(), emptyList(), emptyList()))
        every { pdlClient.hentPerson(any(), any()) } returns personMedKode6

        assertThatThrownBy { service.harTilgang(fnr, "token", "https://url.no/", HttpMethod.POST) }
            .isInstanceOf(ManglendeTilgangException::class.java)
            .hasMessage("Person har addressebeskyttelse.")
    }

    @Test
    internal fun `manglende tilgang til kode7`() {
        val kode7 = listOf(Adressebeskyttelse(Gradering.FORTROLIG))
        val personMedKode7 = PdlHentPerson(PdlPerson(kode7, emptyList(), emptyList(), emptyList(), emptyList()))
        every { pdlClient.hentPerson(any(), any()) } returns personMedKode7

        assertThatThrownBy { service.harTilgang(fnr, "token", "https://url.no/", HttpMethod.POST) }
            .isInstanceOf(ManglendeTilgangException::class.java)
            .hasMessage("Person har addressebeskyttelse.")
    }

    @Test
    internal fun `manglende tilgang til egenAnsatt`() {
        every { skjermedePersonerClient.erPersonSkjermet(any(), any()) } returns true

        assertThatThrownBy { service.harTilgang(fnr, "token", "https://url.no/", HttpMethod.POST) }
            .isInstanceOf(ManglendeTilgangException::class.java)
            .hasMessage("Person er skjermet.")
    }
}
