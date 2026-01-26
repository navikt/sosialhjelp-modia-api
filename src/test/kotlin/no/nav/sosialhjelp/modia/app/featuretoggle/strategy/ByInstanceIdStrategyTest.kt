package no.nav.sosialhjelp.modia.app.featuretoggle.strategy

import io.getunleash.UnleashContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ByInstanceIdStrategyTest {
    private val ctx = UnleashContext.builder().build()

    @Test
    fun shouldReturnFalse_instanceIdNotInMap() {
        val strategy = ByInstanceIdStrategy("local")
        val parameters = mutableMapOf("instance.id" to "dev-fss,dev-gcp")
        assertFalse(strategy.isEnabled(parameters, ctx))
    }

    @Test
    fun shoudReturnTrue_instanceIdInMap() {
        val strategy = ByInstanceIdStrategy("dev-gcp")
        val parameters = mutableMapOf("instance.id" to "dev-fss,dev-gcp")
        assertTrue(strategy.isEnabled(parameters, ctx))
    }
}
