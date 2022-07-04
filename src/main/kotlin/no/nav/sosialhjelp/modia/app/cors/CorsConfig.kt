package no.nav.sosialhjelp.modia.app.cors

import no.nav.sosialhjelp.modia.utils.MiljoUtils
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class CorsConfig(
    private val miljoUtils: MiljoUtils,
) {

//    @Bean
//    fun corsFilter(): FilterRegistrationBean<CorsFilter> {
//        val source = UrlBasedCorsConfigurationSource()
//        val bean = FilterRegistrationBean<CorsFilter>()
//        val config = CorsConfiguration()
//        config.allowedOrigins = allowedOrigins
//        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
//        config.allowedHeaders = listOf("Origin", "Content-Type", "Accept", "X-XSRF-TOKEN", "Authorization", "Nav-Call-Id")
//        config.allowCredentials = true
//        config.maxAge = 3600L
//        source.registerCorsConfiguration("/**", config)
//        val corsFilter = CorsFilter(source)
//        bean.filter = corsFilter
// //        bean.order = Ordered.HIGHEST_PRECEDENCE
//        return bean
//    }

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedOrigins(*allowedOrigins.toTypedArray())
                    .allowedHeaders("Origin", "Content-Type", "Accept", "X-XSRF-TOKEN", "Authorization", "Nav-Call-Id")
                    .allowCredentials(true)
                    .maxAge(3600L)
            }
        }
    }

    private val allowedOrigins: List<String>
        get() = if (isRunningInProd) ALLOWED_ORIGINS_PROD else ALLOWED_ORIGINS_NON_PROD

    private val isRunningInProd get() = miljoUtils.isRunningInProd()

    companion object {
        private val ALLOWED_ORIGINS_PROD = listOf(
            "https://sosialhjelp-modia-api.intern.nav.no"
        )
        private val ALLOWED_ORIGINS_NON_PROD = listOf(
            "https://sosialhjelp-modia-api.dev.intern.nav.no",
            "https://sosialhjelp-modia-api-mock.dev.nav.no",
            "https://sosialhjelp-modia-api.labs.nais.io"
        )
    }
}
