package no.nav.sosialhjelp.modia.app

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt

class AudienceValidator(
    private val audiences: List<String>,
) : OAuth2TokenValidator<Jwt> {
    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
        val tokenAudiences = jwt.audience

        return if (tokenAudiences.any { it in audiences }) {
            OAuth2TokenValidatorResult.success()
        } else {
            OAuth2TokenValidatorResult.failure(
                OAuth2Error(
                    "invalid_token",
                    "The required audience is missing. Expected one of: $audiences, but got: $tokenAudiences",
                    null,
                ),
            )
        }
    }
}
