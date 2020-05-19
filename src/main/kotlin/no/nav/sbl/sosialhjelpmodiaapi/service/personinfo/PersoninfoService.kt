package no.nav.sbl.sosialhjelpmodiaapi.service.personinfo

import no.nav.sbl.sosialhjelpmodiaapi.domain.PersoninfoResponse
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.PdlClient
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.getNavn
import org.springframework.stereotype.Component

@Component
class PersoninfoService(
        private val pdlClient: PdlClient
) {

    fun hentPersoninfo(ident: String): PersoninfoResponse {

        val hentPerson = pdlClient.hentPerson(ident)

        // utvid med alder, fnr, tlfnr etter hvert
        return PersoninfoResponse(
                hentPerson?.getNavn()
        )
    }
}