package no.nav.sbl.sosialhjelpmodiaapi.config

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("!(local | mock | mock-alt")
@Configuration
@EnableJwtTokenValidation(ignore = ["org.springframework", "springfox.documentation.swagger.web.ApiResourceController"])
@EnableOAuth2Client(cacheEnabled = true)
class SecurityConfig {

    // override default fra token-support
    // https://github.com/navikt/token-support/blob/master/token-validation-spring/src/main/java/no/nav/security/token/support/spring/EnableJwtTokenValidationConfiguration.java#L69
    @Primary
    @Bean
    fun proxyAwareResourceRetriever(): ProxyAwareResourceRetriever {
        return ProxyAwareResourceRetriever()
    }
}