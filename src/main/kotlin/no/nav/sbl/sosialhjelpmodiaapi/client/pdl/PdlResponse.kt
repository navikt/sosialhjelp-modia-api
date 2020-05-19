package no.nav.sbl.sosialhjelpmodiaapi.client.pdl

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
        val navn: List<PdlPersonNavn>
)

data class PdlPersonNavn(
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String
)

fun PdlHentPerson.getNavn(): String? {
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

fun String.capitalizeEachWord(): String {
    return this.split(" ").toList()
            .joinToString(separator = " ") { it.toLowerCase().capitalize() }

}