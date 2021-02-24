package no.nav.sosialhjelp.modia.health

import no.nav.sosialhjelp.modia.utils.Miljo
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.SelftestMeterBinder
import no.nav.sosialhjelp.selftest.SelftestService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HealthConfig(
        private val dependencyChecks: List<DependencyCheck>
) {

    @Bean
    fun selftestService(): SelftestService {
        return SelftestService("sosialhjelp-modia-api", Miljo.getAppImage(), dependencyChecks)
    }

    @Bean
    fun selftestMeterBinder(selftestService: SelftestService): SelftestMeterBinder {
        return SelftestMeterBinder(selftestService)
    }
}