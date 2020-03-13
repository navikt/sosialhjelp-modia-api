package no.nav.sbl.sosialhjelpmodiaapi.pdl

interface PdlClient {

    fun hentPerson(ident: String): PdlHentPerson?

    fun ping()
}