package no.nav.sbl.sosialhjelpmodiaapi.client.unleash.strategy

import no.finn.unleash.strategy.Strategy

class ByInstanceIdStrategy : Strategy {
    override fun getName(): String = "byInstanceId"

    override fun isEnabled(parameters: MutableMap<String, String>?): Boolean {
        val instances: String = parameters?.get("instance.id") ?: ""
        val instanceIds: List<String> = instances.split(",\\s*".toRegex())

        return instanceIds.any { isCurrentInstance(it) }
    }

    private fun isCurrentInstance(instance: String): Boolean {
        return System.getProperty(INSTANCE_PROPERTY, "prod-fss") == instance
    }

    companion object {
        private const val INSTANCE_PROPERTY = "unleash_instance_id"
    }
}