package no.nav.sosialhjelp.modia.tilgang

import io.mockk.called
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.app.exceptions.ManglendeModiaSosialhjelpTilgangException
import no.nav.sosialhjelp.modia.app.exceptions.ManglendeTilgangException
import no.nav.sosialhjelp.modia.logging.AuditService
import no.nav.sosialhjelp.modia.person.pdl.Adressebeskyttelse
import no.nav.sosialhjelp.modia.person.pdl.Gradering
import no.nav.sosialhjelp.modia.person.pdl.PdlClient
import no.nav.sosialhjelp.modia.person.pdl.PdlHentPerson
import no.nav.sosialhjelp.modia.person.pdl.PdlPerson
import no.nav.sosialhjelp.modia.tilgang.azure.AzureGraphClient
import no.nav.sosialhjelp.modia.tilgang.azure.model.AzureAdGruppe
import no.nav.sosialhjelp.modia.tilgang.azure.model.AzureAdGrupper
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
    private val azureGraphClient: AzureGraphClient = mockk()
    private val auditService: AuditService = mockk()
    private val clientProperties: ClientProperties = mockk()

    private val service = TilgangskontrollService(pdlClient, skjermedePersonerClient, azureGraphClient, auditService, clientProperties)

    private val fnr = "fnr"
    private val pdlPersonUtenBeskyttelse = PdlHentPerson(PdlPerson(emptyList(), emptyList(), emptyList(), emptyList(), emptyList()))

    private val veilederGruppe = "riktigVeilederGruppe"
    private val feilVeilederGruppe = "feilVeilederGruppe"
    private val azureAdIngenGruppe = AzureAdGrupper(emptyList())
    private val azureAdRiktigGruppe = AzureAdGrupper(listOf(AzureAdGruppe(veilederGruppe, "Riktig gruppe")))
    private val azureAdFeilGruppe = AzureAdGrupper(listOf(AzureAdGruppe(feilVeilederGruppe, "Feil gruppe")))

    @BeforeEach
    internal fun setUp() {
        every { azureGraphClient.hentInnloggetVeilederSineGrupper(any()) } returns azureAdRiktigGruppe
        every { clientProperties.veilederGruppeId } returns veilederGruppe
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
        val azureToken = slot<String>()
        val skjermToken = slot<String>()

        every { azureGraphClient.hentInnloggetVeilederSineGrupper(capture(azureToken)) } returns azureAdRiktigGruppe
        every { skjermedePersonerClient.erPersonSkjermet(fnr, capture(skjermToken)) } returns false

        service.harTilgang(fnr, "$BEARER$tokenString", "https://url.no/", HttpMethod.POST)

        assertThat(azureToken.isCaptured).isTrue
        assertThat(azureToken.captured).isEqualTo(tokenString).doesNotContain(BEARER)
        assertThat(azureToken.captured).doesNotContain(BEARER)
        assertThat(skjermToken.isCaptured).isTrue
        assertThat(skjermToken.captured).isEqualTo(tokenString)
        assertThat(skjermToken.captured).doesNotContain(BEARER)
    }

    @Test
    internal fun `manglende tilgang til sosialhjelp - feil gruppe`() {
        every { azureGraphClient.hentInnloggetVeilederSineGrupper(any()) } returns azureAdFeilGruppe

        assertThatThrownBy { service.harTilgang(fnr, "token", "https://url.no/", HttpMethod.POST) }
            .isInstanceOf(ManglendeModiaSosialhjelpTilgangException::class.java)
            .hasMessage("Veileder er ikke i riktig azure gruppe til å bruke modia sosialhjelp.")
    }

    @Test
    internal fun `manglende tilgang til sosialhjelp - ingen grupper`() {
        every { azureGraphClient.hentInnloggetVeilederSineGrupper(any()) } returns azureAdIngenGruppe

        assertThatThrownBy { service.harTilgang(fnr, "token", "https://url.no/", HttpMethod.POST) }
            .isInstanceOf(ManglendeModiaSosialhjelpTilgangException::class.java)
            .hasMessage("Veileder er ikke i riktig azure gruppe til å bruke modia sosialhjelp.")
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
