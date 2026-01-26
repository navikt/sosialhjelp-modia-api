package no.nav.sosialhjelp.modia.app.client

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "client")
class ClientProperties {
    lateinit var fiksDigisosEndpointUrl: String

    lateinit var fiksIntegrasjonId: String
    lateinit var fiksIntegrasjonpassord: String

    lateinit var norgEndpointUrl: String

    lateinit var pdlEndpointUrl: String
    lateinit var pdlScope: String

    lateinit var skjermedePersonerScope: String
    lateinit var skjermedePersonerEndpointUrl: String
}
