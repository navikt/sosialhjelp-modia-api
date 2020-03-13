package no.nav.sbl.sosialhjelpmodiaapi.common

import kotlinx.coroutines.delay
import kotlin.reflect.KClass

internal suspend fun <T> retry(
        attempts: Int = 10,
        initialDelay: Long = 100L,
        maxDelay: Long = 1000L,
        factor: Double = 2.0,
        vararg retryableExceptions: KClass<out Throwable> = arrayOf(),
        block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(attempts - 1) {
        try {
            return timed { block() }
        } catch (e: Throwable) {
            if (retryableExceptions.none { it.isInstance(e) }) {
                countAndRethrowError(e) {
                }
            }
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return try {
        timed { block() }
    } catch (e: Throwable) {
        countAndRethrowError(e) {
        }
    }
}

private fun countAndRethrowError(e: Throwable, block: () -> Any?): Nothing {
    block()
    throw e
}

internal suspend inline fun <T> timed(crossinline block: suspend () -> T) =
        block()
