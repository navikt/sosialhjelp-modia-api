package no.nav.sosialhjelp.modia.health.selftest

import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class PrettyPrintSelftestConfiguration : WebMvcConfigurer {

    // print selftestsiden (json) på en litt mer lettleselig måte
    override fun extendMessageConverters(converters: List<HttpMessageConverter<*>>) {
        converters
            .filterIsInstance<MappingJackson2HttpMessageConverter>()
            .forEach { it.setPrettyPrint(true) }
    }
}
