package no.nav.sbl.sosialhjelpmodiaapi.subjecthandler

import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

interface SubjectHandler {
    fun getConsumerId(): String
    fun getUserIdFromToken(): String
    fun getToken(): String
}

class StaticSubjectHandlerImpl : SubjectHandler {
    private val DEFAULT_USER = "26104500284" //Testbruker i idporten.
    private val DEFAULT_TOKEN = "token"
    private var user = DEFAULT_USER
    private var token = DEFAULT_TOKEN

    override fun getUserIdFromToken(): String {
        return this.user
    }

    override fun getToken(): String {
        return this.token
    }

    override fun getConsumerId(): String {
        return "StaticConsumerId"
    }

    fun setUser(user: String) {
        this.user = user
    }

    fun setFakeToken(fakeToken: String) {
        this.token = fakeToken
    }

    fun reset() {
        this.user = DEFAULT_USER
        this.token = DEFAULT_TOKEN
    }
}

@Profile("!mock")
@Component
class IssoSubjectHandlerImpl(
        private val tokenValidationContextHolder: TokenValidationContextHolder
) : SubjectHandler {

    companion object {
        private const val ISSUER = "veileder" // todo blir dette riktig?
        private val log by logger()
    }

    private val tokenValidationContext: TokenValidationContext
        get() {
            val tokenValidationContext = tokenValidationContextHolder.tokenValidationContext
            if (tokenValidationContext == null) {
                log.error("Could not find TokenValidationContext. Possibly no token in request and request was not captured by JwtToken-validation filters.")
                throw JwtTokenValidatorException("Could not find TokenValidationContext. Possibly no token in request.")
            }
            return tokenValidationContext
        }

    override fun getUserIdFromToken(): String {
        return tokenValidationContext.getClaims(ISSUER).subject
    }

    override fun getToken(): String {
        return tokenValidationContext.getJwtToken(ISSUER).tokenAsString
    }

    override fun getConsumerId(): String {
        return System.getProperty("consumerid")
    }
}