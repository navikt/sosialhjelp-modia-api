package no.nav.sbl.sosialhjelpmodiaapi.config

import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import java.nio.charset.StandardCharsets

@Configuration
class RestConfig {

    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder
                .additionalMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
                .build()
    }

    @Bean
    @Profile("!(mock | local)")
    fun serviceuserBasicAuthRestTemplate(builder: RestTemplateBuilder): RestTemplate =
            builder
                    .basicAuthentication(System.getenv(SRVSOSIALHJELP_MODIA_API_USERNAME), System.getenv(SRVSOSIALHJELP_MODIA_API_PASSWORD), StandardCharsets.UTF_8)
                    .build()

    @Bean
    fun objectMapperCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { jacksonObjectMapperBuilder ->
            jacksonObjectMapperBuilder.configure(objectMapper)
        }
    }

    @Bean
    fun corsFilter(): CORSFilter {
        return CORSFilter()
    }

    companion object {
        private const val SRVSOSIALHJELP_MODIA_API_USERNAME: String = "SRVSOSIALHJELP_MODIA_API_USERNAME"
        private const val SRVSOSIALHJELP_MODIA_API_PASSWORD: String = "SRVSOSIALHJELP_MODIA_API_PASSWORD"
    }
}
