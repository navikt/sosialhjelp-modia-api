package no.nav.sbl.sosialhjelpmodiaapi.mock

import no.nav.sbl.sosialhjelpmodiaapi.domain.NavEnhet
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.defaultNAVEnhet
import no.nav.sbl.sosialhjelpmodiaapi.norg.NorgClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("mock | local")
@Component
class NorgClientMock : NorgClient {

    private val innsynMap = mutableMapOf<String, NavEnhet>()

    override fun hentNavEnhet(enhetsnr: String): NavEnhet {
        return innsynMap.getOrElse(enhetsnr, {
            val default = defaultNAVEnhet
            innsynMap[enhetsnr] = default
            default
        })
    }

    fun postNavEnhet(enhetsnr: String, navenhet: NavEnhet) {
        innsynMap[enhetsnr] = navenhet
    }
}