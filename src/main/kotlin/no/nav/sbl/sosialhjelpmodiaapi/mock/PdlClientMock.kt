package no.nav.sbl.sosialhjelpmodiaapi.mock

import no.nav.sbl.sosialhjelpmodiaapi.pdl.PdlClient
import no.nav.sbl.sosialhjelpmodiaapi.pdl.PdlHentPerson
import no.nav.sbl.sosialhjelpmodiaapi.pdl.PdlPerson
import no.nav.sbl.sosialhjelpmodiaapi.pdl.PdlPersonNavn
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("mock | local")
class PdlClientMock: PdlClient {

    override fun hentPerson(): PdlHentPerson? {
        return PdlHentPerson(
                PdlPerson(
                        listOf(PdlPersonNavn("Bruce", "mock", "Banner"))
                )
        )
    }

    override fun ping() {

    }

}