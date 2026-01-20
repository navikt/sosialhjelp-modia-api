package no.nav.sosialhjelp.modia.fodselsnummer

import no.nav.sosialhjelp.modia.redis.RedisKeyType
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.utils.sosialhjelpJsonMapper
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class FodselsnummerService(
    private val redisService: RedisService,
) {
    fun setFnrForSalesforce(fnr: String): String {
        val fnrId = UUID.randomUUID().toString()
        lagreTilCache(fnrId, fnr)
        return fnrId
    }

    fun getFnr(fnrId: String): String? = hentFraCache(fnrId)

    private fun lagreTilCache(
        fnrId: String,
        fnr: String,
    ) {
        redisService.set(RedisKeyType.FNR_SERVICE, fnrId, sosialhjelpJsonMapper.writeValueAsBytes(fnr), FNR_TIME_TO_LIVE_SECONDS)
    }

    private fun hentFraCache(fnrId: String): String? = redisService.getString(RedisKeyType.FNR_SERVICE, fnrId)

    companion object {
        private const val FNR_TIME_TO_LIVE_SECONDS = 3600L
    }
}
