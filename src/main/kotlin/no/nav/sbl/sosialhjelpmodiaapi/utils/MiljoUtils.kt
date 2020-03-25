package no.nav.sbl.sosialhjelpmodiaapi.utils

import org.springframework.beans.factory.annotation.Value

object MiljoUtils {

    @Value("\${spring.profiles.active}")
    private val activeProfile: String = "default"

    fun isProfileMockOrLocal(): Boolean {
        return activeProfile == "mock" || activeProfile == "local"
    }
}