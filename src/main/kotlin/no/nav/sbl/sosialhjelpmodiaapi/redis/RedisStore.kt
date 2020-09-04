package no.nav.sbl.sosialhjelpmodiaapi.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisFuture
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit


@Component
class RedisStore @Autowired constructor(redisClient: RedisClient) {

    private final val connection = redisClient.connect()
    private val async = connection.async()!!

    fun get(key: String): String? {
        val get: RedisFuture<String> = async.get(key)
        val await = get.await(1, TimeUnit.SECONDS)
        return if (await) {
            get.get()
        } else null
    }

    fun set(key: String, value: String, timeToLive: Long): String? {
        val set: RedisFuture<String> = async.setex(key, timeToLive, value)
        return if (set.await(1, TimeUnit.SECONDS)) {
            set.get()
        } else null
    }

}