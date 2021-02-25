package no.nav.sosialhjelp.modia.service.navkontor

import no.nav.sosialhjelp.modia.client.norg.NorgClient
import no.nav.sosialhjelp.modia.client.norg.NavEnhet
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.rest.NavKontorinfoController.KontorinfoResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class NavKontorService(
        @Value("\${client.norg_oppslag_url}") private val norg_oppslag_url: String,
        private val norgClient: NorgClient,
        private val redisService: RedisService
) {

    fun hentNavKontorinfo(enhetsnr: String): KontorinfoResponse? {
        val enhet = hentFraCache()?.firstOrNull { it.enhetNr == enhetsnr } ?: norgClient.hentNavEnhet(enhetsnr)
        if (enhet == null || enhet.sosialeTjenester.isNullOrBlank()) {
            return null
        }
        return KontorinfoResponse(enhet.enhetNr, enhet.navn, enhet.sosialeTjenester, lagNorgUrl(enhet.enhetNr))
    }

    fun hentAlleNavKontorinfo(): List<KontorinfoResponse> {
        val alleEnheter = hentFraCache() ?: norgClient.hentAlleNavEnheter()

        if (alleEnheter.isEmpty()) {
            return emptyList()
        }
        return alleEnheter
                .filter { it.type == TYPE_LOKAL }
                .map {
                    KontorinfoResponse(
                            it.enhetNr,
                            it.navn,
                            it.sosialeTjenester ?: "",
                            lagNorgUrl(it.enhetNr)
                    )
                }
    }

    private fun hentFraCache(): List<NavEnhet>? {
        return redisService.getAlleNavEnheter()
    }

    private fun lagNorgUrl(enhetNr: String): String {
        return norg_oppslag_url + enhetNr
    }

    companion object {
        private const val TYPE_LOKAL = "LOKAL"
    }
}
