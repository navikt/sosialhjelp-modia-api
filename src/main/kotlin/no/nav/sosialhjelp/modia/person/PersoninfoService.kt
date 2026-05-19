package no.nav.sosialhjelp.modia.person

import no.nav.sosialhjelp.modia.person.pdl.PdlClient
import no.nav.sosialhjelp.modia.person.pdl.alder
import no.nav.sosialhjelp.modia.person.pdl.kjoenn
import no.nav.sosialhjelp.modia.person.pdl.navn
import no.nav.sosialhjelp.modia.person.pdl.telefonnummer
import no.nav.sosialhjelp.modia.utils.IntegrationUtils
import org.springframework.stereotype.Component

@Component
class PersoninfoService(
    private val pdlClient: PdlClient,
) {
    fun hentPersoninfo(
        ident: String,
        token: String,
    ): PersoninfoResponse {
        val veilederToken = token.replace(IntegrationUtils.BEARER, "")
        val hentPerson = pdlClient.hentPerson(ident, veilederToken)

        return PersoninfoResponse(
            hentPerson?.navn,
            hentPerson?.alder,
            hentPerson?.kjoenn,
            hentPerson?.telefonnummer,
        )
    }
}
