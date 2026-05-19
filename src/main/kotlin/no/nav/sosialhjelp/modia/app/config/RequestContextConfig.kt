package no.nav.sosialhjelp.modia.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.request.RequestContextListener

@Configuration
class RequestContextConfig {
    /**
     * RequestContextListener ensures that request context is available throughout
     * the entire request lifecycle, including async dispatches used by coroutines.
     * This prevents "No thread-bound request found" errors in JWT validation.
     */
    @Bean
    fun requestContextListener(): RequestContextListener = RequestContextListener()
}
