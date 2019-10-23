package no.nav.sbl.sosialhjelpmodiaapi.common

import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

private const val unexpectedError: String = "unexpected_error"
private const val fiksError: String = "fiks_error"
private const val norgError: String = "norg_error"


@ControllerAdvice
class InnsynExceptionHandler : ResponseEntityExceptionHandler() {

    companion object {
        val log by logger()
    }

    @ExceptionHandler(Throwable::class)
    fun handleAll(e: Throwable): ResponseEntity<ErrorMessage> {
        log.error(e.message, e)
        val error = ErrorMessage(unexpectedError, e.message)
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(FiksException::class)
    fun handleFiksError(e: FiksException): ResponseEntity<ErrorMessage> {
        log.error("Noe feilet ved kall til Fiks", e)
        val error = ErrorMessage(fiksError, "Noe uventet feilet")
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(NorgException::class)
    fun handleNorgError(e: NorgException): ResponseEntity<ErrorMessage> {
        log.error("Noe feilet ved kall til Norg", e)
        val error = ErrorMessage(norgError, "Noe uventet feilet")
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(OpplastingException::class)
    fun handleOpplastingError(e: OpplastingException): ResponseEntity<ErrorMessage> {
        log.error("Noe feilet ved opplasting av vedlegg", e)
        val error = ErrorMessage(unexpectedError, "Noe uventet feilet")
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

}

data class ErrorMessage(
        val type: String?,
        val message: String?
)