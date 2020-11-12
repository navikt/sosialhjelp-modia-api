package no.nav.sbl.sosialhjelpmodiaapi.service.navkontor

import no.nav.sbl.sosialhjelpmodiaapi.client.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.KontorinfoResponse
import no.nav.sbl.sosialhjelpmodiaapi.domain.NavKontorResponse
import org.springframework.stereotype.Component

@Component
class NavKontorService(
        private val norgClient: NorgClient // todo: legg til caching
) {

    fun hentNavKontorinfo(enhetsnr: String) : KontorinfoResponse? {
        val enhet = norgClient.hentNavEnhet(enhetsnr)
        if (enhet == null || enhet.sosialeTjenester.isNullOrBlank()) {
            return null
        }
        return KontorinfoResponse(enhet.navn, enhet.sosialeTjenester)
    }

    fun hentAlleNavKontorinfo(): List<NavKontorResponse> {
        val alleEnheter = norgClient.hentAlleNavEnheter()
        if (alleEnheter.isEmpty()) {
            return emptyList()
        }
        return alleEnheter
                .filter { it.type == TYPE_LOKAL }
                .map { NavKontorResponse(it.enhetNr, it.navn, it.sosialeTjenester ?: "") }
    }

    companion object {
        private const val TYPE_LOKAL = "LOKAL"
    }
}