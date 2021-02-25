package no.nav.sosialhjelp.modia.mock

import no.nav.sosialhjelp.modia.client.pdl.Kjoenn
import no.nav.sosialhjelp.modia.client.pdl.PdlClient
import no.nav.sosialhjelp.modia.client.pdl.PdlFoedselsdato
import no.nav.sosialhjelp.modia.client.pdl.PdlHentPerson
import no.nav.sosialhjelp.modia.client.pdl.PdlKjoenn
import no.nav.sosialhjelp.modia.client.pdl.PdlPerson
import no.nav.sosialhjelp.modia.client.pdl.PdlPersonNavn
import no.nav.sosialhjelp.modia.client.pdl.PdlTelefonnummer
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