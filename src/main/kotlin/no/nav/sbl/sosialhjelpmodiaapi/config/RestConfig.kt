package no.nav.sbl.sosialhjelpmodiaapi.config

import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import java.nio.charset.StandardCharsets

@Configuration
class RestConfig {

    companion object {
        private const val SRVSOSIALHJELP_MODIA_API_USERNAME: String = "SRVSOSIALHJELP_MODIA_API_USERNAME"
        private const val SRVSOSIALHJELP_MODIA_API_PASSWORD: String = "SRVSOSIALHJELP_MODIA_API_PASSWORD"
    }

    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate =
            builder.build()

    @Bean
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
    fun myMessageConverter(reqAdapter: RequestMappingHandlerAdapter,
                           jacksonObjectMapperBuilder: Jackson2ObjectMapperBuilder): MappingJackson2HttpMessageConverter {
        // **replace previous MappingJackson converter**
        val converters = reqAdapter.messageConverters
        converters.removeIf { httpMessageConverter -> httpMessageConverter.javaClass == MappingJackson2HttpMessageConverter::class.java }

        val jackson = MappingJackson2HttpMessageConverter(objectMapper)
        converters.add(jackson)
        reqAdapter.messageConverters = converters
        return jackson
    }

}