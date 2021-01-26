package no.nav.sbl.sosialhjelpmodiaapi.redis

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.properties.Delegates

@Profile("redis-cache")
@Component
@ConfigurationProperties(prefix = "modia.cache")
class CacheProperties {

    var timeToLiveSeconds: Long by Delegates.notNull()

}