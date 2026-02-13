package no.nav.sosialhjelp.modia.app.client

import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec
import java.io.IOException
import java.time.Duration

object RetryUtils {
    private const val DEFAULT_MAX_ATTEMPTS: Long = 5
    private const val DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS: Long = 100

    private val DEFAULT_SERVER_ERROR_FILTER: (Throwable) -> (Boolean) = {
        it is WebClientResponseException && it.statusCode.is5xxServerError
    }

    /**
     * Filter for retrying on connection errors (e.g., "Connection reset by peer").
     * These errors can occur when idle connections are closed by the server but the client hasn't detected it yet.
     */
    private val CONNECTION_ERROR_FILTER: (Throwable) -> (Boolean) = {
        when {
            // WebClientRequestException wraps connection-level errors like IOException
            it is WebClientRequestException && it.cause is IOException -> true
            // Direct IOException (e.g., "Connection reset by peer")
            it is IOException -> true
            else -> false
        }
    }

    fun retryBackoffSpec(
        predicate: (Throwable) -> (Boolean) = DEFAULT_SERVER_ERROR_FILTER,
        maxAttempts: Long = DEFAULT_MAX_ATTEMPTS,
        initialWaitIntervalMillis: Long = DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
    ): RetryBackoffSpec =
        Retry
            .backoff(maxAttempts, Duration.ofMillis(initialWaitIntervalMillis))
            .filter { predicate(it) }

    /**
     * Retry specification for both server errors (5xx) and connection errors.
     * Use this for HTTP clients that may encounter stale connection issues after periods of inactivity.
     */
    fun retryBackoffSpecWithConnectionErrors(
        maxAttempts: Long = DEFAULT_MAX_ATTEMPTS,
        initialWaitIntervalMillis: Long = DEFAULT_INITIAL_WAIT_INTERVAL_MILLIS,
    ): RetryBackoffSpec =
        Retry
            .backoff(maxAttempts, Duration.ofMillis(initialWaitIntervalMillis))
            .filter { DEFAULT_SERVER_ERROR_FILTER(it) || CONNECTION_ERROR_FILTER(it) }
}
