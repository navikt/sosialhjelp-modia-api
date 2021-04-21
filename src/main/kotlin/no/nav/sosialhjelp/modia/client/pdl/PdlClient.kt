package no.nav.sosialhjelp.modia.client.pdl

interface PdlClient {

    fun hentPerson(ident: String): PdlHentPerson?

    fun ping()
}
