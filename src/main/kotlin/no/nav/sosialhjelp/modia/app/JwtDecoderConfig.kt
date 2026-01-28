package no.nav.sosialhjelp.modia.app

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

@Profile("gcp&!mock-alt")
@Configuration
class JwtDecoderConfig {
    @Bean
    fun jwtDecoder(
        @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}") issuerUri: String,
        @Value("\${spring.security.oauth2.resourceserver.jwt.audiences}") audiences: String,
    ): JwtDecoder {
        val jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri) as NimbusJwtDecoder

        val audienceValidator = AudienceValidator(audiences.split(","))
        val withIssuer: OAuth2TokenValidator<Jwt> = JwtValidators.createDefaultWithIssuer(issuerUri)
        val withAudience: OAuth2TokenValidator<Jwt> = DelegatingOAuth2TokenValidator(withIssuer, audienceValidator)

        jwtDecoder.setJwtValidator(withAudience)
        return jwtDecoder
    }
}
