package no.nav.sosialhjelp.modia.app.cors

import no.nav.sosialhjelp.modia.utils.MiljoUtils
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Component
class CorsConfig(
    private val miljoUtils: MiljoUtils
) {

    @Bean
    fun corsFilter(): FilterRegistrationBean<CorsFilter> {
        val source = UrlBasedCorsConfigurationSource()
        val bean = FilterRegistrationBean<CorsFilter>()
        val config = CorsConfiguration()
        config.allowedOrigins = allowedOrigins()
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        config.allowedHeaders = listOf("Origin", "Content-Type", "Accept", "X-XSRF-TOKEN", "Authorization", "Nav-Call-Id")
        config.allowCredentials = isRunningInProd
        config.maxAge = 3600L
        config.addAllowedHeader("Content-type")
        source.registerCorsConfiguration("/**", config)
        val corsFilter = CorsFilter(source)
        bean.filter = corsFilter
        bean.order = Ordered.HIGHEST_PRECEDENCE
        return bean
    }

    private fun allowedOrigins(): List<String> {
        return if (isRunningInProd) ALLOWED_ORIGINS_PROD else ALLOWED_ORIGINS_NON_PROD
    }

    private val isRunningInProd get() = miljoUtils.isRunningInProd()

    companion object {
        private val ALLOWED_ORIGINS_PROD = listOf(
            "https://sosialhjelp-modia-api.intern.nav.no"
        )
        private val ALLOWED_ORIGINS_NON_PROD = listOf("*")
    }
}