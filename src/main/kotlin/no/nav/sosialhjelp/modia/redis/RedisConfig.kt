package no.nav.sosialhjelp.modia.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!no-redis")
@Configuration
@EnableConfigurationProperties(RedisProperties::class)
class RedisConfig {

    @Bean
    fun redisClient(properties: RedisProperties): RedisClient {
        val redisUri = RedisURI.create(properties.host, properties.port)
        redisUri.setPassword(properties.password as CharSequence)

        return RedisClient.create(redisUri)
    }

}