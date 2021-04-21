package no.nav.sosialhjelp.modia.redis

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import kotlin.properties.Delegates

@Component
@ConfigurationProperties(prefix = "modia.cache")
class CacheProperties {

    var timeToLiveSeconds: Long by Delegates.notNull()
}
