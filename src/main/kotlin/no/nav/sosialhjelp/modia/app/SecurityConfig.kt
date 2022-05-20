package no.nav.sosialhjelp.modia.app

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!mock-alt")
@Configuration
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc.webmvc.api.OpenApiWebMvcResource", "org.springdoc.webmvc.ui.SwaggerWelcomeWebMvc"])
@EnableOAuth2Client(cacheEnabled = true)
class SecurityConfig {

    // JwtTokenValidation er enabled så lenge appen kjører med profil != mock-alt

    @Bean
    fun servletWebServerFactory(): ServletWebServerFactory {
        return JettyServletWebServerFactory()
    }
}
