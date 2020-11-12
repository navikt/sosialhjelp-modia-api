package no.nav.sbl.sosialhjelpmodiaapi.service.navkontor

import no.nav.sbl.sosialhjelpmodiaapi.client.norg.NorgClient
import no.nav.sbl.sosialhjelpmodiaapi.domain.KontorinfoResponse
import no.nav.sbl.sosialhjelpmodiaapi.domain.NavKontorResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class NavKontorService(
        @Value("\${client.norg_oppslag_url}") private val norg_oppslag_url: String,
        private val norgClient: NorgClient, // todo: legg til caching
) {

    fun hentNavKontorinfo(enhetsnr: String) : KontorinfoResponse? {
        val enhet = norgClient.hentNavEnhet(enhetsnr)
        if (enhet == null || enhet.sosialeTjenester.isNullOrBlank()) {
            return null
        }
        return KontorinfoResponse(enhet.navn, enhet.sosialeTjenester, lagNorgUrl(enhet.enhetNr))
    }

    private fun lagNorgUrl(enhetNr: String): String {
        return norg_oppslag_url + enhetNr
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
