package no.nav.sbl.sosialhjelpmodiaapi.health.selftest

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.timelimiter.TimeLimiter
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import io.vavr.control.Try
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import no.nav.sbl.sosialhjelpmodiaapi.logger
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger


abstract class AbstractDependencyCheck(
        protected val type: DependencyType,
        private val name: String,
        protected val address: String,
        private val importance: Importance) {

    companion object {
        val log by logger()
    }

    private val circuitBreaker = CircuitBreaker.ofDefaults("selftest")
    private val dispatcher: ExecutorCoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val timeLimiterConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofMillis(1000))
            .cancelRunningFuture(true)
            .build()
    private val timeLimiter = TimeLimiter.of(timeLimiterConfig)

    private val dependencyStatus = AtomicInteger()

    protected abstract fun doCheck()

    fun check(): Try<DependencyCheckResult> {
        // fixme change to suspend function?
        val callable: Callable<DependencyCheckResult> = dispatcher.run { getCheckCallable() }
//        val susp: suspend () -> DependencyCheckResult = suspend {  getCheck() }

        // decorate suspend function
//        val timeRestrictedCall: suspend () -> DependencyCheckResult = timeLimiter.decorateSuspendFunction(susp)

//        val chainedCallable2: suspend () -> DependencyCheckResult = circuitBreaker.decorateSuspendFunction(susp)
        val chainedCallable: Callable<DependencyCheckResult> = CircuitBreaker.decorateCallable(circuitBreaker, callable)

        return Try
                .ofCallable(chainedCallable)
                .onSuccess { dependencyStatus.set(1) }
                .onFailure { dependencyStatus.set(0) }
                .recover { throwable ->
                    log.warn("Call to dependency={} with type={} at url={} timed out or circuitbreaker was tripped.", name, type, address, throwable)
                    DependencyCheckResult(
                            endpoint = name,
                            result = if (importance == Importance.CRITICAL) Result.ERROR else Result.WARNING,
                            address = address,
                            errorMessage = "Call to dependency=$name timed out or circuitbreaker tripped. Errormessage=${getErrorMessageFromThrowable(throwable)}",
                            type = type,
                            importance = importance,
                            responseTime = null,
                            throwable = throwable)
                }
    }

    private fun getCheckCallable(): Callable<DependencyCheckResult> {
        return Callable {
            val start = Instant.now()
            doCheck()
            val end = Instant.now()
            val responseTime = Duration.between(start, end).toMillis()

            DependencyCheckResult(
                    type = type,
                    endpoint = name,
                    importance = importance,
                    address = address,
                    result = Result.OK,
                    errorMessage = null,
                    responseTime = "$responseTime ms",
                    throwable = null
            )
        }
    }

    private fun getErrorMessageFromThrowable(e: Throwable): String? {
        if (e is TimeoutException) {
            return "Call to dependency timed out by circuitbreaker"
        }
        return if (e.cause == null) e.message else e.cause!!.message
    }
}