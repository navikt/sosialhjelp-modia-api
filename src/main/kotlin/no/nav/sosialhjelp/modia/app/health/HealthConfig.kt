package no.nav.sosialhjelp.modia.app.health

import io.micrometer.core.instrument.MeterRegistry
import no.nav.sosialhjelp.modia.utils.Miljo
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.SelftestService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HealthConfig(
    private val dependencyChecks: List<DependencyCheck>,
    private val meterRegistry: MeterRegistry,
) {
    @Bean
    fun selftestService(): SelftestService = SelftestService("sosialhjelp-modia-api", Miljo.getAppImage(), dependencyChecks, meterRegistry)
}
