package no.nav.sosialhjelp.modia.config

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!(mock | mock-alt)")
@Configuration
@EnableJwtTokenValidation(ignore = ["org.springframework", "springfox.documentation.swagger.web.ApiResourceController", "springfox.documentation.oas.web.OpenApiControllerWebMvc"])
@EnableOAuth2Client(cacheEnabled = true)
class SecurityConfig {

    // JwtTokenValidation er enabled så lenge appen kjører med profil != mock(-alt)
}
