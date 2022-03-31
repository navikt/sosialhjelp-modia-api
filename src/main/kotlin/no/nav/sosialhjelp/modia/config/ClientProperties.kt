package no.nav.sosialhjelp.modia.config

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

    lateinit var stsTokenEndpointUrl: String

    lateinit var veilederGruppeId: String

    lateinit var bergenKommunenummer: String
    lateinit var stavangerKommunenummer: String

    lateinit var azureTokenEndpointUrl: String
    lateinit var azureClientId: String
    lateinit var azureClientSecret: String
    lateinit var azuredingsUrl: String
    lateinit var azuredingsJwtClientId: String
    lateinit var azuredingsJwtAudience: String
    lateinit var azuredingsPrivateJwk: String
    lateinit var azureGraphUrl: String

    lateinit var skjermedePersonerScope: String
    lateinit var skjermedePersonerEndpointUrl: String

    lateinit var unleashUrl: String
    lateinit var unleashInstanceId: String
}
