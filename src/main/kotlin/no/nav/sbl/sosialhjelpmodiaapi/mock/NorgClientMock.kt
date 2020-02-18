package no.nav.sbl.sosialhjelpmodiaapi.mock

import no.nav.sbl.sosialhjelpmodiaapi.domain.NavEnhet
import no.nav.sbl.sosialhjelpmodiaapi.norg.NorgClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("mock | local")
@Component
class NorgClientMock : NorgClient {

    private val innsynMap = mutableMapOf<String, NavEnhet>()

    override fun hentNavEnhet(enhetsnr: String): NavEnhet {
        return innsynMap.getOrElse(enhetsnr, {
            val default = NavEnhet(
                    enhetId = 100000367,
                    navn = "NAV Longyearbyen",
                    enhetNr = enhetsnr,
                    antallRessurser = 20,
                    status = "AKTIV",
                    aktiveringsdato = "1982-04-21",
                    nedleggelsesdato = "null",
                    sosialeTjenester = "Informasjon om sosiale tjenester for enhetsnr $enhetsnr. \nGjerne over flere linjer.\n\tKanskje til og med tab.\nOg mere til!"
            )
            innsynMap[enhetsnr] = default
            default
        })
    }
}