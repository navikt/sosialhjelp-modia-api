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
    private val miljoUtils: MiljoUtils,
) {
    @Bean
    fun corsFilter(): FilterRegistrationBean<CorsFilter> {
        val source = UrlBasedCorsConfigurationSource()
        val bean = FilterRegistrationBean<CorsFilter>()
        val config = CorsConfiguration()
        config.allowedOrigins = allowedOrigins
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        config.allowedHeaders = listOf("Origin", "Content-Type", "Accept", "X-XSRF-TOKEN", "Authorization", "Nav-Call-Id")
        config.allowCredentials = true
        config.maxAge = 3600L
        source.registerCorsConfiguration("/**", config)
        val corsFilter = CorsFilter(source)
        bean.filter = corsFilter
        bean.order = Ordered.HIGHEST_PRECEDENCE
        return bean
    }

    private val allowedOrigins: List<String>
        get() = if (miljoUtils.isRunningInProd()) ALLOWED_ORIGINS_PROD else ALLOWED_ORIGINS_NON_PROD

    companion object {
        private val ALLOWED_ORIGINS_PROD =
            listOf(
                "https://sosialhjelp-modia.intern.nav.no",
                "https://navdialog.lightning.force.com",
            )
        private val ALLOWED_ORIGINS_NON_PROD =
            listOf(
                "https://sosialhjelp-modia.intern.dev.nav.no",
                "https://sosialhjelp-modia-dev.dev.nav.no",
                "https://digisos.dev.nav.no",
                "https://digisos.ekstern.dev.nav.no/sosialhjelp/modia",
                "https://navdialog--sit2.sandbox.lightning.force.com",
            )
    }
}
