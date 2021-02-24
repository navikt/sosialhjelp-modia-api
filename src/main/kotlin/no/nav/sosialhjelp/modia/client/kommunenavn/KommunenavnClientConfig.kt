package no.nav.sosialhjelp.modia.client.kommunenavn

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class KommunenavnClientConfig {

    @Bean
    fun kommuneClient(restTemplate: RestTemplate): KommunenavnClient {
        return KommunenavnClient(restTemplate = restTemplate)
    }
}
