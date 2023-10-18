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
    private val clientProperties: ClientProperties
) {

    @Bean
    fun unleashClient(): Unleash {
        val byInstanceIdStrategy = ByInstanceIdStrategy(clientProperties.unleash_env)
        val config = UnleashConfig.builder()
            .appName(clientProperties.unleash_instance_id)
            .environment(clientProperties.unleash_env)
            .instanceId(clientProperties.unleash_instance_id + "_" + clientProperties.unleash_env)
            .unleashAPI("${clientProperties.unleash_server_api_url}/api")
            .apiKey(clientProperties.unleash_server_api_token)
            .build()

        return DefaultUnleash(
            config,
            byInstanceIdStrategy
        )
    }
}

@Profile("local")
@Configuration
class UnleashMockConfig {

    @Bean
    fun unleashClient(): Unleash {
        return FakeUnleash()
    }
}
