package no.nav.sbl.sosialhjelpmodiaapi.mock

import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.Kjoenn
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.PdlClient
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.PdlFoedselsdato
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.PdlHentPerson
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.PdlKjoenn
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.PdlPerson
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.PdlPersonNavn
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.PdlTelefonnummer
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("mock | local")
class PdlClientMock : PdlClient {

    private val pdlHentPersonMap = mutableMapOf<String, PdlHentPerson>()

    override fun hentPerson(ident: String): PdlHentPerson? {
        return pdlHentPersonMap.getOrElse(ident, {
            val default = defaultPdlHentPerson()
            pdlHentPersonMap[ident] = default
            default
        })
    }

    private fun defaultPdlHentPerson(): PdlHentPerson {
        return PdlHentPerson(
                PdlPerson(
                        listOf(PdlPersonNavn("Bruce", "mock", "Banner")),
                        listOf(PdlKjoenn(Kjoenn.KVINNE)),
                        listOf(PdlFoedselsdato("2000-01-01")),
                        listOf(PdlTelefonnummer("+47", "12345678", 1))
                )
        )
    }

    override fun ping() {

    }

}