package no.nav.sbl.sosialhjelpmodiaapi.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisFuture
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Profile("!no-redis")
@Component
class RedisStore(
    redisClient: RedisClient
) {

    private val connection: StatefulRedisConnection<String, ByteArray> = redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE))
    private val async: RedisAsyncCommands<String, ByteArray> = connection.async()!!

    fun get(key: String): ByteArray? {
        val redisFuture: RedisFuture<ByteArray> = async.get(key)
        val await = redisFuture.await(1, TimeUnit.SECONDS)
        return if (await) {
            redisFuture.get()
        } else null
    }

    fun set(key: String, value: ByteArray, timeToLive: Long): String? {
        val redisFuture: RedisFuture<String> = async.setex(key, timeToLive, value)
        return if (redisFuture.await(1, TimeUnit.SECONDS)) {
            redisFuture.get()
        } else null
    }
}
