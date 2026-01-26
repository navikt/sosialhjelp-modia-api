package no.nav.sosialhjelp.modia.utils

import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class MiljoUtils(
    val env: Environment,
) {
    fun isRunningInProd(): Boolean = "prod" in env.activeProfiles
}

object Miljo {
    const val SRVSOSIALHJELP_MOD = "srvsosialhjelp-mod"
}
