package no.nav.sbl.sosialhjelpmodiaapi.client.unleash.strategy

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ByInstanceIdStrategyTest {

    @Test
    fun shouldReturnFalse_instanceIdNotInMap() {
        val strategy = ByInstanceIdStrategy("local")
        val parameters = mutableMapOf("instance.id" to "dev-fss,dev-gcp")
        assertFalse(strategy.isEnabled(parameters))
    }

    @Test
    fun shoudReturnTrue_instanceIdInMap() {
        val strategy = ByInstanceIdStrategy("dev-gcp")
        val parameters = mutableMapOf("instance.id" to "dev-fss,dev-gcp")
        assertTrue(strategy.isEnabled(parameters))
    }
}
