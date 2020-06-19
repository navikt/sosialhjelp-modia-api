package no.nav.sbl.sosialhjelpmodiaapi.common

import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


@ControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {

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
        log.error("DigisosSak id: \"${e.digisosId}\" finnes ikke i FIKS: ${e.message}")
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
//        log.error("Noe feilet ved kall til Pdl", e)
        val error = FrontendErrorMessage(PDL_ERROR, "Noe uventet feilet")
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(TilgangskontrollException::class)
    fun handleTilgangskontrollException(e: TilgangskontrollException): ResponseEntity<FrontendErrorMessage> {
        log.warn("Abac - Ingen tilgang til ressurs", e)
        val error = FrontendErrorMessage(INGEN_TILGANG, "Ingen tilgang til ressurs")
        return ResponseEntity(error, HttpStatus.FORBIDDEN)
    }

    companion object {
        private val log by logger()

        private const val UNEXPECTED_ERROR = "unexpected_error"
        private const val FIKS_ERROR = "fiks_error"
        private const val NORG_ERROR = "norg_error"
        private const val PDL_ERROR = "pdl_error"
        private const val INGEN_TILGANG = "ingen_tilgang"
    }

    data class FrontendErrorMessage(
            val type: String?,
            val message: String?
    )
}
