package no.nav.sbl.sosialhjelpmodiaapi.config

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!(mock | mock-alt")
@Configuration
@EnableJwtTokenValidation(ignore=["org.springframework", "springfox.documentation.swagger.web.ApiResourceController"])
class JwtTokenValidationConfig
