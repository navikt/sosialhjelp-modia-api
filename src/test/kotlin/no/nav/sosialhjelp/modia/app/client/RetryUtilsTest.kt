package no.nav.sosialhjelp.modia.app.client

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.io.IOException

class RetryUtilsTest {
    @Test
    fun `retryBackoffSpec should retry on 5xx errors`() {
        var attempts = 0
        val maxAttempts = 3L

        val mono =
            Mono
                .fromCallable<String> {
                    attempts++
                    throw WebClientResponseException.create(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        null,
                        null,
                        null,
                    )
                }.retryWhen(RetryUtils.retryBackoffSpec(maxAttempts = maxAttempts, initialWaitIntervalMillis = 1))

        StepVerifier
            .create(mono)
            .expectError()
            .verify()

        assertEquals(maxAttempts + 1, attempts.toLong())
    }

    @Test
    fun `retryBackoffSpec should not retry on 4xx errors`() {
        var attempts = 0

        val mono =
            Mono
                .fromCallable<String> {
                    attempts++
                    throw WebClientResponseException.create(
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        null,
                        null,
                        null,
                    )
                }.retryWhen(RetryUtils.retryBackoffSpec(maxAttempts = 3, initialWaitIntervalMillis = 1))

        StepVerifier
            .create(mono)
            .expectError(WebClientResponseException::class.java)
            .verify()

        assertEquals(1, attempts)
    }

    @Test
    fun `retryBackoffSpecWithConnectionErrors should retry on connection reset errors`() {
        var attempts = 0
        val maxAttempts = 3L

        val mono =
            Mono
                .fromCallable<String> {
                    attempts++
                    // Simulate "Connection reset by peer" error
                    throw WebClientRequestException(
                        IOException("Connection reset by peer"),
                        null,
                        null,
                        null,
                    )
                }.retryWhen(
                    RetryUtils.retryBackoffSpecWithConnectionErrors(
                        maxAttempts = maxAttempts,
                        initialWaitIntervalMillis = 1,
                    ),
                )

        StepVerifier
            .create(mono)
            .expectError()
            .verify()

        assertEquals(maxAttempts + 1, attempts.toLong())
    }

    @Test
    fun `retryBackoffSpecWithConnectionErrors should retry on IOException`() {
        var attempts = 0
        val maxAttempts = 3L

        val mono =
            Mono
                .fromCallable<String> {
                    attempts++
                    throw IOException("recvAddress(..) failed with error(-104): Connection reset by peer")
                }.retryWhen(
                    RetryUtils.retryBackoffSpecWithConnectionErrors(
                        maxAttempts = maxAttempts,
                        initialWaitIntervalMillis = 1,
                    ),
                )

        StepVerifier
            .create(mono)
            .expectError()
            .verify()

        assertEquals(maxAttempts + 1, attempts.toLong())
    }

    @Test
    fun `retryBackoffSpecWithConnectionErrors should retry on 5xx errors`() {
        var attempts = 0
        val maxAttempts = 3L

        val mono =
            Mono
                .fromCallable<String> {
                    attempts++
                    throw WebClientResponseException.create(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        null,
                        null,
                        null,
                    )
                }.retryWhen(
                    RetryUtils.retryBackoffSpecWithConnectionErrors(
                        maxAttempts = maxAttempts,
                        initialWaitIntervalMillis = 1,
                    ),
                )

        StepVerifier
            .create(mono)
            .expectError()
            .verify()

        assertEquals(maxAttempts + 1, attempts.toLong())
    }

    @Test
    fun `retryBackoffSpecWithConnectionErrors should not retry on 4xx errors`() {
        var attempts = 0

        val mono =
            Mono
                .fromCallable<String> {
                    attempts++
                    throw WebClientResponseException.create(
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        null,
                        null,
                        null,
                    )
                }.retryWhen(
                    RetryUtils.retryBackoffSpecWithConnectionErrors(
                        maxAttempts = 3,
                        initialWaitIntervalMillis = 1,
                    ),
                )

        StepVerifier
            .create(mono)
            .expectError(WebClientResponseException::class.java)
            .verify()

        assertEquals(1, attempts)
    }

    @Test
    fun `retryBackoffSpecWithConnectionErrors should succeed after transient connection error`() {
        var attempts = 0

        val mono =
            Mono
                .fromCallable {
                    attempts++
                    if (attempts == 1) {
                        // First attempt fails with connection reset
                        throw IOException("Connection reset by peer")
                    }
                    // Second attempt succeeds
                    "Success"
                }.retryWhen(
                    RetryUtils.retryBackoffSpecWithConnectionErrors(
                        maxAttempts = 3,
                        initialWaitIntervalMillis = 1,
                    ),
                )

        StepVerifier
            .create(mono)
            .expectNext("Success")
            .verifyComplete()

        assertEquals(2, attempts)
    }
}
