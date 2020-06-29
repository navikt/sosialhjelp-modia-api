package no.nav.sbl.sosialhjelpmodiaapi.utils

import net.bytebuddy.implementation.bytecode.Throw
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
    private const val TESTBRUKER_NATALIE = "TESTBRUKER_NATALIE"

    fun getAppImage(): String {
        return getEnvVariable(NAIS_APP_IMAGE, "version")
    }

    fun getTestbrukerNatalie(): String {
        return getEnvVariable(TESTBRUKER_NATALIE, "11111111111")
    }

    private fun getEnvVariable(key: String, default: String): String {
        return try {
            System.getenv(key)
        } catch (t: Throwable) {
            default
        }
    }

}
