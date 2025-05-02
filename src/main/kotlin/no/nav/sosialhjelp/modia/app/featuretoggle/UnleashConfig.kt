package no.nav.sosialhjelp.modia.app.featuretoggle

import io.getunleash.DefaultUnleash
import io.getunleash.FakeUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.app.featuretoggle.strategy.ByInstanceIdStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!local")
@Configuration
class UnleashConfig(
    private val clientProperties: ClientProperties,
) {
    @Bean
    fun unleashClient(): Unleash {
        val byInstanceIdStrategy = ByInstanceIdStrategy(clientProperties.unleashEnv)
        val config =
            UnleashConfig
                .builder()
                .appName(clientProperties.unleashInstanceId)
                .environment(clientProperties.unleashEnv)
                .instanceId(clientProperties.unleashInstanceId + "_" + clientProperties.unleashEnv)
                .unleashAPI("${clientProperties.unleashServerApiUrl}/api")
                .apiKey(clientProperties.unleashServerApiToken)
                .build()

        return DefaultUnleash(
            config,
            byInstanceIdStrategy,
        )
    }
}

@Profile("local")
@Configuration
class UnleashMockConfig {
    @Bean
    fun unleashClient(): Unleash = FakeUnleash()
}
