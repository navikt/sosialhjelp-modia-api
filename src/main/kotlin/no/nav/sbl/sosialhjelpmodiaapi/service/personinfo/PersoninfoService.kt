package no.nav.sbl.sosialhjelpmodiaapi.service.personinfo

import no.nav.sbl.sosialhjelpmodiaapi.domain.PersoninfoResponse
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.PdlClient
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.alder
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.kjoenn
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.navn
import no.nav.sbl.sosialhjelpmodiaapi.client.pdl.telefonnummer
import org.springframework.stereotype.Component

@Component
class PersoninfoService(
        private val pdlClient: PdlClient
) {

    fun hentPersoninfo(ident: String): PersoninfoResponse {

        val hentPerson = pdlClient.hentPerson(ident)

        // utvid med alder, fnr, tlfnr etter hvert
        return PersoninfoResponse(
                hentPerson?.navn,
                hentPerson?.alder,
                hentPerson?.kjoenn,
                hentPerson?.telefonnummer
        )
    }
}