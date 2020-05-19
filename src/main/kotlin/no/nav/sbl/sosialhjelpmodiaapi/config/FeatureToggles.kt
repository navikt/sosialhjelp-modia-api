package no.nav.sbl.sosialhjelpmodiaapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import kotlin.properties.Delegates

@Component
@ConfigurationProperties(prefix = "modia.features")
class FeatureToggles {

//    var auditlogEnabled: Boolean by Delegates.notNull()
}