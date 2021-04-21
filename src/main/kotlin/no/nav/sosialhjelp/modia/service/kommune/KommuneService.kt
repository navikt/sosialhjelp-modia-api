package no.nav.sosialhjelp.modia.service.kommune

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.service.idporten.IdPortenService
import no.nav.sosialhjelp.modia.utils.objectMapper
import org.springframework.stereotype.Component

@Component
class KommuneService(
    private val kommuneInfoClient: KommuneInfoClient,
    private val idPortenService: IdPortenService,
    private val redisService: RedisService
) {

    fun get(kommunenummer: String): KommuneInfo {
        hentFraCache(kommunenummer)?.let { return it }

        return kommuneInfoClient.get(kommunenummer, getToken())
            .also { lagreTilCache(it) }
    }

    fun getBehandlingsanvarligKommune(kommunenummer: String): String? {
        val behandlingsansvarlig = get(kommunenummer).behandlingsansvarlig

        return if (behandlingsansvarlig != null) leggTilKommuneINavnet(behandlingsansvarlig) else null
    }

    private fun lagreTilCache(kommuneInfo: KommuneInfo) {
        redisService.set(kommuneInfo.kommunenummer, objectMapper.writeValueAsBytes(kommuneInfo))
    }

    private fun leggTilKommuneINavnet(kommunenavn: String): String {
        return if (kommunenavn.toLowerCase().endsWith(" kommune")) kommunenavn else "$kommunenavn kommune"
    }

    fun getAll(): List<KommuneInfo> {
        return kommuneInfoClient.getAll(getToken())
    }

    private fun getToken(): String {
        return idPortenService.getToken().token
    }

    private fun hentFraCache(kommunenummer: String): KommuneInfo? {
        return redisService.get(kommunenummer, KommuneInfo::class.java) as KommuneInfo?
    }
}
