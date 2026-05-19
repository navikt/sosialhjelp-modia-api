package no.nav.sosialhjelp.modia.app.client

import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec
import java.time.Duration

object RetryUtils {
    private const val DEFAULT_MAX_ATTEMPTS: Long = 5
    private const val DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS: Long = 100

    private val DEFAULT_SERVER_ERROR_FILTER: (Throwable) -> (Boolean) = {
        it is WebClientResponseException && it.statusCode.is5xxServerError
    }

    fun retryBackoffSpec(
        predicate: (Throwable) -> (Boolean) = DEFAULT_SERVER_ERROR_FILTER,
        maxAttempts: Long = DEFAULT_MAX_ATTEMPTS,
        initialWaitIntervalMillis: Long = DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
    ): RetryBackoffSpec =
        Retry
            .backoff(maxAttempts, Duration.ofMillis(initialWaitIntervalMillis))
            .filter { predicate(it) }
}
