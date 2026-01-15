package no.nav.sosialhjelp.modia.utils

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class MiljoUtils(
    val env: Environment,
) {
    fun isRunningInProd(): Boolean = PROD_FSS in env.activeProfiles || ("gcp" in env.activeProfiles && "prod" in env.activeProfiles)

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
