package no.nav.sosialhjelp.modia.app.exceptions

import no.nav.security.token.support.core.exceptions.IssuerConfigurationException
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.core.exceptions.MetaDataNotAvailableException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.api.fiks.exceptions.FiksNotFoundException
import no.nav.sosialhjelp.modia.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {
    @param:Value("\${loginurl}")
    private val loginurl: String? = null

    @ExceptionHandler(Throwable::class)
    fun handleAll(e: Throwable): ResponseEntity<FrontendErrorMessage> {
        log.error(e.message, e)
        val error = FrontendErrorMessage(UNEXPECTED_ERROR, e.message)
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(FiksException::class)
    fun handleFiksError(e: FiksException): ResponseEntity<FrontendErrorMessage> {
        log.error("Noe feilet ved kall til Fiks", e)
        val error = FrontendErrorMessage(FIKS_ERROR, "Noe uventet feilet")
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(FiksNotFoundException::class)
    fun handleFiksNotFoundError(e: FiksNotFoundException): ResponseEntity<FrontendErrorMessage> {
        log.error("DigisosSak finnes ikke i FIKS: ${e.message}")
        val error = FrontendErrorMessage(FIKS_ERROR, "DigisosSak finnes ikke")
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(NorgException::class)
    fun handleNorgError(e: NorgException): ResponseEntity<FrontendErrorMessage> {
        log.error("Noe feilet ved kall til Norg", e)
        val error = FrontendErrorMessage(NORG_ERROR, "Noe uventet feilet")
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(PdlException::class)
    fun handlePdlError(e: PdlException): ResponseEntity<FrontendErrorMessage> {
        log.error("Noe feilet ved kall til Pdl", e)
        val error = FrontendErrorMessage(PDL_ERROR, "Noe uventet feilet")
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(MsGraphException::class)
    fun handleMsGraphException(e: MsGraphException): ResponseEntity<FrontendErrorMessage> {
        log.warn("MsGraph - noe feilet", e)
        val error = FrontendErrorMessage(MSGRAPH_ERROR, "Noe uventet feilet")
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(ManglendeTilgangException::class)
    fun handleManglendeTilgangException(e: ManglendeTilgangException): ResponseEntity<FrontendErrorMessage> {
        log.info("ManglendeTilgangException: ${e.message}")
        val error = FrontendErrorMessage(TILGANG_ERROR, "Mangler tilgang til bruker")
        return ResponseEntity(error, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(ManglendeModiaSosialhjelpTilgangException::class)
    fun handleManglendeModiaSosialhjelpTilgangException(
        e: ManglendeModiaSosialhjelpTilgangException,
    ): ResponseEntity<FrontendErrorMessage> {
        log.info("Veileder manger ad-rolle for tilgang til sosialhjelp i modia.")
        val error = FrontendErrorMessage(TILGANG_ERROR, "Mangler tilgang til tjenesten")
        return ResponseEntity(error, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(value = [JwtTokenUnauthorizedException::class, JwtTokenMissingException::class])
    fun handleTokenValidationExceptions(
        ex: RuntimeException,
        request: WebRequest,
    ): ResponseEntity<FrontendErrorMessage> {
        if (ex.message?.contains("Server misconfigured") == true) {
            log.error(ex.message)
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(FrontendErrorMessage("unexpected_error", "Noe uventet feilet"))
        }
        log.info("Bruker er ikke autentisert mot AzureAD (enda). Sender 401 med loginurl. Feilmelding: ${ex.message}")
        return createUnauthorizedWithLoginUrlResponse(loginurl!!)
    }

    @ExceptionHandler(value = [MetaDataNotAvailableException::class, IssuerConfigurationException::class])
    fun handleTokenValidationConfigurationExceptions(
        ex: RuntimeException,
        request: WebRequest,
    ): ResponseEntity<FrontendErrorMessage> {
        log.error("Klarer ikke hente metadata fra discoveryurl eller problemer ved konfigurering av issuer. Feilmelding: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(FrontendErrorMessage("unexpected_error", "Noe uventet feilet"))
    }

    private fun createUnauthorizedWithLoginUrlResponse(loginUrl: String): ResponseEntity<FrontendErrorMessage> =
        ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(FrontendUnauthorizedMelding(loginUrl, "authentication_error", "Autentiseringsfeil"))

    companion object {
        private val log by logger()

        private const val UNEXPECTED_ERROR = "unexpected_error"
        private const val FIKS_ERROR = "fiks_error"
        private const val NORG_ERROR = "norg_error"
        private const val PDL_ERROR = "pdl_error"
        private const val MSGRAPH_ERROR = "msgraph_error"
        private const val TILGANG_ERROR = "tilgang_error"
    }

    open class FrontendErrorMessage(
        val type: String?,
        val message: String?,
    )

    @Suppress("unused")
    class FrontendUnauthorizedMelding(
        val loginUrl: String,
        type: String?,
        message: String?,
    ) : FrontendErrorMessage(type, message)
}
