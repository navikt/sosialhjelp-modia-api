package no.nav.sbl.sosialhjelpmodiaapi.personinfo

import no.nav.sbl.sosialhjelpmodiaapi.domain.PersoninfoResponse
import no.nav.sbl.sosialhjelpmodiaapi.pdl.PdlClient
import no.nav.sbl.sosialhjelpmodiaapi.pdl.getNavn
import org.springframework.stereotype.Component

@Component
class PersoninfoService(private val pdlClient: PdlClient) {

    fun hentPersoninfo(ident: String): PersoninfoResponse {

        val hentPerson = pdlClient.hentPerson(ident)

        // utvid med alder, fnr, tlfnr etter hvert
        return PersoninfoResponse(
                hentPerson?.getNavn()
        )
    }
}