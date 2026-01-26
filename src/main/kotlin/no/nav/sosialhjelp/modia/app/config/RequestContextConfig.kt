package no.nav.sosialhjelp.modia.app.config

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.filter.RequestContextFilter

@Configuration
class RequestContextConfig {

    @Bean
    fun requestContextFilter(): FilterRegistrationBean<RequestContextFilter> {
        val filterRegistration = FilterRegistrationBean<RequestContextFilter>()
        filterRegistration.filter = RequestContextFilter()
        // Set order to run before CORS filter (which uses HIGHEST_PRECEDENCE)
        filterRegistration.order = Ordered.HIGHEST_PRECEDENCE - 1
        filterRegistration.addUrlPatterns("/*")
        return filterRegistration
    }
}
