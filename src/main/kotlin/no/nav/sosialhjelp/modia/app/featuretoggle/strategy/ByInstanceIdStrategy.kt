package no.nav.sosialhjelp.modia.app.featuretoggle.strategy

import io.getunleash.UnleashContext
import io.getunleash.strategy.Strategy

class ByInstanceIdStrategy(
    private val currentInstanceId: String,
) : Strategy {
    override fun getName(): String = "byInstanceId"

    override fun isEnabled(
        parameters: MutableMap<String, String>,
        ctx: UnleashContext,
    ): Boolean {
        val instances: String = parameters.get("instance.id") ?: ""
        val instanceIds: List<String> = instances.split(",\\s*".toRegex())

        return instanceIds.any { it == currentInstanceId }
    }
}
