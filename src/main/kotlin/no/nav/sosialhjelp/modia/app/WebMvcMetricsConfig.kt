package no.nav.sosialhjelp.modia.app

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsProvider
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import javax.servlet.DispatcherType

@Configuration
class WebMvcMetricsConfig {

    @Bean
    fun webMvcMetricsFilter(
        registry: MeterRegistry,
        tagsProvider: WebMvcTagsProvider,
        metricsProperties: MetricsProperties
    ): FilterRegistrationBean<WebMvcMetricsFilter> {
        val request: MetricsProperties.Web.Server.ServerRequest = metricsProperties.web.server.request
        val filter = WebMvcMetricsFilter(registry, tagsProvider, request.metricName, request.autotime)
        val registration = FilterRegistrationBean(filter)
        registration.order = Ordered.HIGHEST_PRECEDENCE + 1
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC)
        return registration
    }
}
