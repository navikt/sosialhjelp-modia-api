package no.nav.sbl.sosialhjelpmodiaapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "client")
class ClientProperties {

    lateinit var abacPdpEndpointUrl: String

    lateinit var fiksDigisosEndpointUrl: String

    lateinit var fiksIntegrasjonId: String
    lateinit var fiksIntegrasjonIdKommune: String

    lateinit var fiksIntegrasjonpassord: String
    lateinit var fiksIntegrasjonPassordKommune: String

    lateinit var norgEndpointUrl: String

    lateinit var idPortenTokenUrl: String
    lateinit var idPortenClientId: String
    lateinit var idPortenScope: String
    lateinit var idPortenConfigUrl: String

}