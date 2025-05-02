package no.nav.sosialhjelp.modia.kommune

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.modia.kommune.fiks.KommuneInfoClient
import no.nav.sosialhjelp.modia.redis.RedisKeyType
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.utils.objectMapper
import org.springframework.stereotype.Component

@Component
class KommuneService(
    private val kommuneInfoClient: KommuneInfoClient,
    private val redisService: RedisService,
) {
    fun get(kommunenummer: String): KommuneInfo {
        hentFraCache(kommunenummer)?.let { return it }

        return kommuneInfoClient
            .getKommuneInfo(kommunenummer)
            .also { lagreTilCache(it) }
    }

    fun getBehandlingsanvarligKommune(kommunenummer: String): String? {
        val behandlingsansvarlig = get(kommunenummer).behandlingsansvarlig

        return if (behandlingsansvarlig != null) leggTilKommuneINavnet(behandlingsansvarlig) else null
    }

    private fun lagreTilCache(kommuneInfo: KommuneInfo) {
        redisService.set(RedisKeyType.KOMMUNE_SERVICE, kommuneInfo.kommunenummer, objectMapper.writeValueAsBytes(kommuneInfo))
    }

    private fun leggTilKommuneINavnet(kommunenavn: String): String =
        if (kommunenavn.lowercase().endsWith(" kommune")) kommunenavn else "$kommunenavn kommune"

    private fun hentFraCache(kommunenummer: String): KommuneInfo? =
        redisService.get(RedisKeyType.KOMMUNE_SERVICE, kommunenummer, KommuneInfo::class.java)
}
