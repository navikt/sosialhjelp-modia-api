package no.nav.sbl.sosialhjelpmodiaapi.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(RedisProperties::class)
class RedisConfig(
        private val cacheProperties: CacheProperties
) {

    @Bean
    fun redisClient(properties: RedisProperties): RedisClient {
        cacheProperties.startInMemoryRedisIfMocked()

        val redisUri = RedisURI.create(properties.host, properties.port)
        if (properties.password == null) {
            log.info("redis.password er null");
        } else {
            log.info("redis.password har så mange tegn: " + properties.password.length);
        }
        redisUri.setPassword(properties.password)

        return RedisClient.create(redisUri)
    }

    companion object {
        private val log by logger()
    }
}