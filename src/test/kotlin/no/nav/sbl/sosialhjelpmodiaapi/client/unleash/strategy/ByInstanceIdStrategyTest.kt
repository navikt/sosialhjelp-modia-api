package no.nav.sbl.sosialhjelpmodiaapi.client.unleash.strategy

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ByInstanceIdStrategyTest {

    private val strategy = ByInstanceIdStrategy()

    @AfterEach
    internal fun tearDown() {
        System.clearProperty("unleash_instance_id")
    }

    @Test
    fun shouldReturnFalse_instanceIdNotInMap() {
        System.setProperty("unleash_instance_id", "local")
        val parameters = mutableMapOf("instance.id" to "dev-fss,dev-gcp")
        assertFalse(strategy.isEnabled(parameters))
    }

    @Test
    fun shoudReturnTrue_instanceIdInMap() {
        System.setProperty("unleash_instance_id", "dev-gcp")
        val parameters = mutableMapOf("instance.id" to "dev-fss,dev-gcp")
        assertTrue(strategy.isEnabled(parameters))
    }
}