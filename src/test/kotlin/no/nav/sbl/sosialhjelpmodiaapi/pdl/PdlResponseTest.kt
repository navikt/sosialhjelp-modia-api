package no.nav.sbl.sosialhjelpmodiaapi.pdl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PdlResponseTest {

    private val fornavn = "Fornavn"
    private val mellomnavn = "Mellomnavn"
    private val dobbeltmellomnavn = "Mellom1 Mellom2"
    private val etternavn = "Etternavn"

    @Test
    internal fun `getNavn - uten mellomnavn`() {
        val pdlHentPerson = PdlHentPerson(PdlPerson(listOf(PdlPersonNavn(fornavn, null, etternavn))))

        val navn = pdlHentPerson.getNavn()

        assertThat(navn).isEqualTo("$fornavn $etternavn")
    }

    @Test
    internal fun `getNavn - med mellomnavn`() {
        val pdlHentPerson = PdlHentPerson(PdlPerson(listOf(PdlPersonNavn(fornavn, mellomnavn, etternavn))))

        val navn = pdlHentPerson.getNavn()

        assertThat(navn).isEqualTo("$fornavn $mellomnavn $etternavn")
    }

    @Test
    internal fun `getNavn - skal gi stor forbokstav`() {
        val pdlHentPerson = PdlHentPerson(PdlPerson(listOf(PdlPersonNavn(fornavn.toLowerCase(), null, etternavn.toLowerCase()))))

        val navn = pdlHentPerson.getNavn()

        assertThat(navn).isEqualTo("$fornavn $etternavn")
    }

    @Test
    internal fun `getNavn - skal gi stor forbokstav - med dobbelt navn`() {
        val pdlHentPerson = PdlHentPerson(PdlPerson(listOf(PdlPersonNavn(fornavn.toLowerCase(), dobbeltmellomnavn.toLowerCase(), etternavn.toLowerCase()))))

        val navn = pdlHentPerson.getNavn()

        assertThat(navn).contains("$fornavn $dobbeltmellomnavn $etternavn")
    }

    @Test
    internal fun `getNavn - tom navneliste gir null`() {
        val pdlHentPerson = PdlHentPerson(PdlPerson(emptyList()))

        val navn = pdlHentPerson.getNavn()

        assertThat(navn).isNull()
    }

}