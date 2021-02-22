package no.nav.sbl.sosialhjelpmodiaapi.client.pdl

interface PdlClient {

    fun hentPerson(ident: String): PdlHentPerson?

    fun ping()
}
