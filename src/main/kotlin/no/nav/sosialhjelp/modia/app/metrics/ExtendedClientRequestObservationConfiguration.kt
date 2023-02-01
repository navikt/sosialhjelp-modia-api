package no.nav.sosialhjelp.modia.app.metrics

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ExtendedClientRequestObservationConfiguration {

    @Bean
    fun extendedClientRequestObservationConvention(): ExtendedClientRequestObservationConvention {
        return ExtendedClientRequestObservationConvention()
    }
}
