package no.nav.sosialhjelp.modia.app

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor
import org.springframework.web.context.request.RequestContextHolder

@Configuration
class HendelseFoldExecutorConfig {
    @Bean
    fun hendelseFoldThreadPool(meterRegistry: MeterRegistry): ThreadPoolTaskExecutor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 2
            maxPoolSize = 4
            queueCapacity = 50
            setThreadNamePrefix("hendelse-fold-")
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(5)
            setTaskDecorator { task ->
                val submittingMdc = MDC.getCopyOfContextMap()
                val submittingContext = RequestContextHolder.getRequestAttributes()
                Runnable {
                    val workerMdc = MDC.getCopyOfContextMap()
                    val workerContext = RequestContextHolder.getRequestAttributes()
                    try {
                        if (submittingMdc == null) {
                            MDC.clear()
                        } else {
                            MDC.setContextMap(submittingMdc)
                        }
                        if (submittingContext == null) {
                            RequestContextHolder.resetRequestAttributes()
                        } else {
                            RequestContextHolder.setRequestAttributes(submittingContext)
                        }
                        task.run()
                    } finally {
                        if (workerMdc == null) {
                            MDC.clear()
                        } else {
                            MDC.setContextMap(workerMdc)
                        }
                        if (workerContext == null) {
                            RequestContextHolder.resetRequestAttributes()
                        } else {
                            RequestContextHolder.setRequestAttributes(workerContext)
                        }
                    }
                }
            }
            setRejectedExecutionHandler { _, _ ->
                meterRegistry.counter("hendelser_fold_total", "result", "rejected").increment()
            }
        }

    @Bean("hendelseFoldExecutor")
    fun hendelseFoldExecutor(
        @Qualifier("hendelseFoldThreadPool") executor: AsyncTaskExecutor,
    ): AsyncTaskExecutor = DelegatingSecurityContextAsyncTaskExecutor(executor)
}
