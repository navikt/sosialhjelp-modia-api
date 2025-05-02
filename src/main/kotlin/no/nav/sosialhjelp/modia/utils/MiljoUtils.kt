package no.nav.sosialhjelp.modia.utils

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MiljoUtils {
    @Value("\${spring.profiles.active}")
    private var activeProfile: String = PROD_FSS // default verdi er prod-fss

    fun isRunningInProd(): Boolean = activeProfile.contains(PROD_FSS)

    companion object {
        private const val PROD_FSS = "prod-fss"
    }
}

object Miljo {
    const val SRVSOSIALHJELP_MOD = "srvsosialhjelp-mod"

    private const val NAIS_APP_IMAGE = "NAIS_APP_IMAGE"

    fun getAppImage(): String = getEnvVariable(NAIS_APP_IMAGE, "version")

    private fun getEnvVariable(
        key: String,
        default: String,
    ): String =
        try {
            System.getenv(key)
        } catch (t: Throwable) {
            default
        }
}
