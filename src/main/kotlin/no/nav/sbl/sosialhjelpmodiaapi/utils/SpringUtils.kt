package no.nav.sbl.sosialhjelpmodiaapi.utils

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SpringUtils(
        @Value("\${spring.profiles.active}")
        private val activeProfile: String
) {

    fun isProfileMockOrLocal(): Boolean {
        return activeProfile == "mock" || activeProfile == "local"
    }

}