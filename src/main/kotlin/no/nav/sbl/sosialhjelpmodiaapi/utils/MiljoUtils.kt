package no.nav.sbl.sosialhjelpmodiaapi.utils

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MiljoUtils {

    companion object {
        private const val LOCAL = "local"
        private const val MOCK = "mock"
        private const val PROD_FSS = "prod-fss"
    }

    @Value("\${spring.profiles.active}")
    private var activeProfile: String = PROD_FSS // default verdi er prod-fss

    fun isProfileMockOrLocal(): Boolean {
        return activeProfile == MOCK || activeProfile == LOCAL
    }

    fun isRunningInProd(): Boolean {
        return activeProfile == PROD_FSS
    }

}
