package no.nav.sbl.sosialhjelpmodiaapi.service.kommune

import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.redis.RedisService
import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import org.springframework.stereotype.Component

@Component
class KommuneService(
        private val kommuneInfoClient: KommuneInfoClient,
        private val redisService: RedisService
) {


    fun get(kommunenummer: String): KommuneInfo {
        hentFraCache(kommunenummer)?.let { return it }

        val kommuneInfo = kommuneInfoClient.get(kommunenummer)
        redisService.put(kommunenummer, objectMapper.writeValueAsBytes(kommuneInfo))
        return kommuneInfo
    }

    fun getAll(): List<KommuneInfo> {
        return kommuneInfoClient.getAll()
    }

    private fun hentFraCache(kommunenummer: String): KommuneInfo? {
        return redisService.get(kommunenummer, KommuneInfo::class.java) as KommuneInfo?
    }

    companion object {
        private val log by logger()
    }
}
