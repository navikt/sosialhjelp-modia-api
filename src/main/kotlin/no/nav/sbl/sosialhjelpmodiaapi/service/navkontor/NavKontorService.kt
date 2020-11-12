package no.nav.sbl.sosialhjelpmodiaapi.service.navkontor

import no.nav.sbl.sosialhjelpmodiaapi.client.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.KontorNavnResponse
import no.nav.sbl.sosialhjelpmodiaapi.domain.KontorinfoResponse
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

    fun hentAlleNavKontorinfo(): List<KontorNavnResponse> {
        val alleEnheter = norgClient.hentAlleNavEnheter()
        if (alleEnheter.isEmpty()) {
            return emptyList()
        }
        return alleEnheter
                .filter { it.type == TYPE_LOKAL }
                .map { KontorNavnResponse(it.enhetNr, it.navn) }
    }

    companion object {
        private const val TYPE_LOKAL = "LOKAL"
    }
}