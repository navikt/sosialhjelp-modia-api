package no.nav.sbl.sosialhjelpmodiaapi.pdl

interface PdlClient {

    fun hentPerson(): PdlHentPerson?

    fun ping()
}