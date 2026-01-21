package no.nav.sosialhjelp.modia.navkontor

import no.nav.sosialhjelp.modia.navkontor.norg.NavEnhet
import no.nav.sosialhjelp.modia.navkontor.norg.NorgClient
import no.nav.sosialhjelp.modia.redis.RedisService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class NavKontorService(
    @param:Value("\${client.norg_oppslag_url}") private val norgOppslagUrl: String,
    private val norgClient: NorgClient,
    private val redisService: RedisService,
) {
    fun hentNavKontorinfo(enhetsnr: String): KontorinfoResponse? {
        val enhet = norgClient.hentNavEnhet(enhetsnr)
        if (enhet == null || enhet.sosialeTjenester.isNullOrBlank()) {
            return null
        }
        return KontorinfoResponse(enhet.enhetNr, enhet.navn, enhet.sosialeTjenester, lagNorgUrl(enhet.enhetNr))
    }

    fun hentAlleNavKontorinfo(): List<KontorinfoResponse> {
        val alleEnheter = hentNavEnhetListeFraCache() ?: norgClient.hentAlleNavEnheter()

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
                    lagNorgUrl(it.enhetNr),
                )
            }
    }

    private fun hentNavEnhetListeFraCache(): List<NavEnhet>? = redisService.getAlleNavEnheter()

    private fun lagNorgUrl(enhetNr: String): String = "$norgOppslagUrl$enhetNr"

    companion object {
        private const val TYPE_LOKAL = "LOKAL"
    }
}
