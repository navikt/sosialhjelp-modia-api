package no.nav.sosialhjelp.modia.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!no-redis")
@Configuration
@EnableConfigurationProperties(DataRedisProperties::class)
class RedisConfig {
    @Bean
    @Profile("gcp")
    fun redisClient(properties: DataRedisProperties): RedisClient {
        val redisUri =
            RedisURI.Builder
                .redis(properties.host, properties.port)
                .withAuthentication(properties.username, properties.password)
                .withSsl(true)
                .build()

        return RedisClient.create(redisUri)
    }
    @Bean
    @Profile("!gcp")
    fun redisClientFss(properties: DataRedisProperties): RedisClient {
        val redisUri =
            RedisURI.Builder
                .redis(properties.host, properties.port)
                .withPassword(properties.password as CharSequence)
                .build()

        return RedisClient.create(redisUri)
    }
}
