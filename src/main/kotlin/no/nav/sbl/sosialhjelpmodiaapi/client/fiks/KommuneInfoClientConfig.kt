package no.nav.sbl.sosialhjelpmodiaapi.client.fiks

import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sosialhjelp.client.kommuneinfo.FiksProperties
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClientImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.client.RestTemplate

@Profile("!mock")
@Configuration
class KommuneInfoClientConfig {

    @Bean
    fun kommuneInfoClient(restTemplate: RestTemplate, clientProperties: ClientProperties): KommuneInfoClient {
        return KommuneInfoClientImpl(
            restTemplate,
            toFiksProperties(clientProperties)
        )
    }

    private fun toFiksProperties(clientProperties: ClientProperties): FiksProperties {
        return FiksProperties(
            clientProperties.fiksDigisosEndpointUrl + FiksPaths.PATH_KOMMUNEINFO,
            clientProperties.fiksDigisosEndpointUrl + FiksPaths.PATH_ALLE_KOMMUNEINFO,
            clientProperties.fiksIntegrasjonId,
            clientProperties.fiksIntegrasjonpassord
        )
    }
}
