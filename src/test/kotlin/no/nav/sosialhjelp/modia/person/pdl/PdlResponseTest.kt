package no.nav.sosialhjelp.modia.person.pdl

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PdlResponseTest {
    private val fornavn = "Fornavn"
    private val mellomnavn = "Mellomnavn"
    private val dobbeltmellomnavn = "Mellom1 Mellom2"
    private val etternavn = "Etternavn"

    private val pdlHentPerson: PdlHentPerson = mockk()

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
    }

    @Test
    internal fun `navn - uten mellomnavn`() {
        every { pdlHentPerson.hentPerson?.navn } returns listOf(PdlPersonNavn(fornavn, null, etternavn))

        val navn = pdlHentPerson.navn

        assertThat(navn).isEqualTo("$fornavn $etternavn")
    }

    @Test
    internal fun `navn - med mellomnavn`() {
        every { pdlHentPerson.hentPerson?.navn } returns listOf(PdlPersonNavn(fornavn, mellomnavn, etternavn))

        val navn = pdlHentPerson.navn

        assertThat(navn).isEqualTo("$fornavn $mellomnavn $etternavn")
    }

    @Test
    internal fun `navn - skal gi stor forbokstav`() {
        every { pdlHentPerson.hentPerson?.navn } returns listOf(PdlPersonNavn(fornavn.lowercase(), null, etternavn.lowercase()))

        val navn = pdlHentPerson.navn

        assertThat(navn).isEqualTo("$fornavn $etternavn")
    }

    @Test
    internal fun `navn - skal gi stor forbokstav - med dobbelt navn`() {
        every { pdlHentPerson.hentPerson?.navn } returns
            listOf(PdlPersonNavn(fornavn.lowercase(), dobbeltmellomnavn.lowercase(), etternavn.lowercase()))

        val navn = pdlHentPerson.navn

        assertThat(navn).contains("$fornavn $dobbeltmellomnavn $etternavn")
    }

    @Test
    internal fun `navn - tom navneliste gir null`() {
        every { pdlHentPerson.hentPerson?.navn } returns emptyList()

        val navn = pdlHentPerson.navn

        assertThat(navn).isNull()
    }

    @Test
    internal fun `alder - tom foedselsdato gir null`() {
        every { pdlHentPerson.hentPerson?.foedselsdato } returns listOf(PdlFoedselsdato(null, null))

        assertThat(pdlHentPerson.alder).isNull()
    }

    @Test
    internal fun `alder - returnerer brukers alder fra foedselsdato`() {
        every { pdlHentPerson.hentPerson?.foedselsdato } returns listOf(PdlFoedselsdato("2000-01-01", null))

        assertThat(pdlHentPerson.alder).isGreaterThan(0)
    }

    @Test
    internal fun `kjoenn - returnerer brukers kjonn`() {
        every { pdlHentPerson.hentPerson?.kjoenn } returns listOf(PdlKjoenn(Kjoenn.KVINNE))

        assertThat(pdlHentPerson.kjoenn).isEqualTo(Kjoenn.KVINNE.name)
    }

    @Test
    internal fun `telefonnummer - returnerer brukers prioriterte telefonnummer`() {
        every { pdlHentPerson.hentPerson?.telefonnummer } returns
            listOf(
                PdlTelefonnummer("+1", "12345678", 2),
                PdlTelefonnummer("+2", "98765432", 1),
            )

        assertThat(pdlHentPerson.telefonnummer).isEqualTo("+298765432")
    }
}
