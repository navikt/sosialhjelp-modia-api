package no.nav.sbl.sosialhjelpmodiaapi.utils

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MiljoUtils {

    @Value("\${spring.profiles.active}")
    private var activeProfile: String = PROD_FSS // default verdi er prod-fss

    fun isProfileMockOrLocal(): Boolean {
        return activeProfile == MOCK || activeProfile == LOCAL
    }

    fun isRunningInProd(): Boolean {
        return activeProfile == PROD_FSS
    }

    companion object {
        private const val LOCAL = "local"
        private const val MOCK = "mock"
        private const val PROD_FSS = "prod-fss"
    }
}

object Miljo {
    private const val NAIS_APP_IMAGE = "NAIS_APP_IMAGE"

    fun getAppImage(): String {
        return try {
            System.getenv(NAIS_APP_IMAGE)
        } catch (e: Exception) {
            "version"
        }
    }

}
