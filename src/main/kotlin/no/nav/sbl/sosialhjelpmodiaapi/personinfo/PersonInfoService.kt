package no.nav.sbl.sosialhjelpmodiaapi.personinfo

import no.nav.sbl.sosialhjelpmodiaapi.domain.PersonInfoResponse
import no.nav.sbl.sosialhjelpmodiaapi.pdl.PdlClient
import no.nav.sbl.sosialhjelpmodiaapi.pdl.getNavn
import org.springframework.stereotype.Component

@Component
class PersonInfoService(private val pdlClient: PdlClient) {

    fun hentPersonInfo(ident: String): PersonInfoResponse {

        val hentPerson = pdlClient.hentPerson(ident)

        // utvid med alder, fnr, tlfnr etter hvert
        return PersonInfoResponse(
                hentPerson?.getNavn()
        )
    }
}