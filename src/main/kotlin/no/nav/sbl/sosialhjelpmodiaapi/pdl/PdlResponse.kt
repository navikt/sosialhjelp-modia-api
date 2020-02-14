package no.nav.sbl.sosialhjelpmodiaapi.pdl

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
        val fornavn = it.fornavn.toLowerCase().capitalize()
        val mellomnavn = it.mellomnavn
        val etternavn = it.etternavn.toLowerCase().capitalize()

        return if (mellomnavn.isNullOrBlank()) {
            "$fornavn $etternavn"
        } else {
            "$fornavn ${mellomnavn.toLowerCase().capitalize()} $etternavn"
        }
    }
}