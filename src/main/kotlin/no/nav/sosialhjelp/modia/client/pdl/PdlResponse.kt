package no.nav.sosialhjelp.modia.client.pdl

import java.time.LocalDate
import java.time.Period
import java.util.Locale

data class PdlPersonResponse(
    val errors: List<PdlError>?,
    val data: PdlHentPerson?
)

data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation>,
    val path: List<String>?,
    val extensions: PdlErrorExtension
)

data class PdlErrorLocation(
    val line: Int?,
    val column: Int?
)

data class PdlErrorExtension(
    val code: String?,
    val classification: String
)

data class PdlHentPerson(
    val hentPerson: PdlPerson?
)

data class PdlPerson(
    val adressebeskyttelse: List<Adressebeskyttelse>,
    val navn: List<PdlPersonNavn>,
    val kjoenn: List<PdlKjoenn>,
    val foedsel: List<PdlFoedselsdato>,
    val telefonnummer: List<PdlTelefonnummer>
)

data class Adressebeskyttelse(
    val gradering: Gradering
)

@Suppress("unused")
enum class Gradering {
    STRENGT_FORTROLIG_UTLAND, // kode 6 (utland)
    STRENGT_FORTROLIG, // kode 6
    FORTROLIG, // kode 7
    UGRADERT
}

data class PdlPersonNavn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String
)

data class PdlKjoenn(
    val kjoenn: Kjoenn
)

data class PdlFoedselsdato(
    val foedselsdato: String?
)

data class PdlTelefonnummer(
    val landskode: String,
    val nummer: String,
    val prioritet: Int
)

@Suppress("unused")
enum class Kjoenn { MANN, KVINNE, UKJENT }

val PdlHentPerson.navn: String?
    get() {
        val navneListe = this.hentPerson?.navn
        if (navneListe.isNullOrEmpty()) {
            return null
        }
        navneListe[0].let {
            val fornavn = it.fornavn.capitalizeEachWord()
            val mellomnavn = it.mellomnavn?.capitalizeEachWord()
            val etternavn = it.etternavn.capitalizeEachWord()

            return if (mellomnavn.isNullOrBlank()) {
                "$fornavn $etternavn"
            } else {
                "$fornavn $mellomnavn $etternavn"
            }
        }
    }

private fun String.capitalizeEachWord(): String {
    return this.split(" ").joinToString(separator = " ") { s -> s.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) } }
}

val PdlHentPerson.alder: Int?
    get() {
        return hentPerson?.foedsel?.firstOrNull()?.foedselsdato?.let { Period.between(LocalDate.parse(it), LocalDate.now()).years }
    }

val PdlHentPerson.kjoenn: String
    get() {
        return hentPerson?.kjoenn?.firstOrNull()?.kjoenn.toString()
    }

val PdlHentPerson.telefonnummer: String?
    get() {
        return hentPerson?.telefonnummer
            ?.minByOrNull { it.prioritet }
            ?.let { it.landskode.plus(it.nummer) }
    }

fun PdlPerson.isKode6Or7(): Boolean {
    return adressebeskyttelse.any {
        it.isKode6() || it.isKode7()
    }
}

fun Adressebeskyttelse.isKode6(): Boolean {
    return this.gradering == Gradering.STRENGT_FORTROLIG || this.gradering == Gradering.STRENGT_FORTROLIG_UTLAND
}

fun Adressebeskyttelse.isKode7(): Boolean {
    return this.gradering == Gradering.FORTROLIG
}
