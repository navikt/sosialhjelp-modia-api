package no.nav.sbl.sosialhjelpmodiaapi.mock

import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.PdlClient
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.PdlHentPerson
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.PdlPerson
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.PdlPersonNavn
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
                        listOf(PdlPersonNavn("Bruce", "mock", "Banner"))
                )
        )
    }

    override fun ping() {

    }

}