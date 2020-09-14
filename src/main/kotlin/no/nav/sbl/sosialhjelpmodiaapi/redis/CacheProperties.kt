package no.nav.sbl.sosialhjelpmodiaapi.redis

import no.nav.sbl.sosialhjelpmodiaapi.redis.RedisMockUtil.startRedisMocked
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import kotlin.properties.Delegates

@Component
@ConfigurationProperties(prefix = "modia.cache")
class CacheProperties {

    var redisMocked: Boolean by Delegates.notNull()

    var timeToLiveSeconds: Long by Delegates.notNull()

    fun startInMemoryRedisIfMocked() {
        if (redisMocked) {
            startRedisMocked()
        }
    }
}