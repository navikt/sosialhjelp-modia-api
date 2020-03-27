package no.nav.sbl.sosialhjelpmodiaapi.utils

import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MiljoUtils {

    companion object {
        private const val DEFAULT = "default-profile"
        private const val LOCAL = "local"
        private const val MOCK = "mock"
        private const val PROD_FSS = "prod-fss"

        private val log by logger()
    }

    @Value("\${spring.profiles.active}")
    private var activeProfile: String = DEFAULT

    fun isProfileMockOrLocal(): Boolean {
        if (activeProfile == DEFAULT) {
            log.warn("Ingen aktiv spring profile? I miljø skal aktiv profil være \${NAIS_CLUSTER_NAME}")
            return false
        }
        return activeProfile == MOCK || activeProfile == LOCAL
    }

    fun isRunningInProd(): Boolean {
        if (activeProfile == DEFAULT) {
            log.warn("Ingen aktiv spring profile? I miljø skal aktiv profil være \${NAIS_CLUSTER_NAME}")
            return false
        }
        return activeProfile == PROD_FSS
    }


}