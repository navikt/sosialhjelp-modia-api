package no.nav.sosialhjelp.modia.app

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!mock-alt")
@Configuration
@EnableJwtTokenValidation(
    ignore = [
        "org.springframework",
        "org.springdoc.webmvc.api.OpenApiWebMvcResource",
        "org.springdoc.webmvc.ui.SwaggerWelcomeWebMvc",
        "org.springdoc.webmvc.ui.SwaggerConfigResource",
    ],
)
class SecurityConfig {
    // JwtTokenValidation er enabled så lenge appen kjører med profil != mock-alt
}
