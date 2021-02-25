package no.nav.sosialhjelp.modia.service.personinfo

import no.nav.sosialhjelp.modia.client.pdl.PdlClient
import no.nav.sosialhjelp.modia.client.pdl.alder
import no.nav.sosialhjelp.modia.client.pdl.kjoenn
import no.nav.sosialhjelp.modia.client.pdl.navn
import no.nav.sosialhjelp.modia.client.pdl.telefonnummer
import no.nav.sosialhjelp.modia.rest.PersoninfoController.PersoninfoResponse
import org.springframework.stereotype.Component

@Component
class PersoninfoService(
        private val pdlClient: PdlClient
) {

    fun hentPersoninfo(ident: String): PersoninfoResponse {
        val hentPerson = pdlClient.hentPerson(ident)

        return PersoninfoResponse(
                hentPerson?.navn,
                hentPerson?.alder,
                hentPerson?.kjoenn,
                hentPerson?.telefonnummer
        )
    }
}