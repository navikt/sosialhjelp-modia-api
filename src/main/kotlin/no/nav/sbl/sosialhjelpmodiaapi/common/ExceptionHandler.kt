package no.nav.sbl.sosialhjelpmodiaapi.common

import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


@ControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {

    companion object {
        val log by logger()

        private const val UNEXPECTED_ERROR = "unexpected_error"
        private const val FIKS_ERROR = "fiks_error"
        private const val NORG_ERROR = "norg_error"
        private const val PDL_ERROR = "pdl_error"
    }

    @ExceptionHandler(Throwable::class)
    fun handleAll(e: Throwable): ResponseEntity<ErrorMessage> {
        log.error(e.message, e)
        val error = ErrorMessage(UNEXPECTED_ERROR, e.message)
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(FiksException::class)
    fun handleFiksError(e: FiksException): ResponseEntity<ErrorMessage> {
        log.error("Noe feilet ved kall til Fiks", e)
        val error = ErrorMessage(FIKS_ERROR, "Noe uventet feilet")
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(FiksNotFoundException::class)
    fun handleFiksNotFoundError(e: FiksNotFoundException): ResponseEntity<ErrorMessage> {
        log.error("DigisosSak finnes ikke i FIKS ", e)
        val error = ErrorMessage(FIKS_ERROR, "DigisosSak finnes ikke")
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(NorgException::class)
    fun handleNorgError(e: NorgException): ResponseEntity<ErrorMessage> {
        log.error("Noe feilet ved kall til Norg", e)
        val error = ErrorMessage(NORG_ERROR, "Noe uventet feilet")
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(PdlException::class)
    fun handlePdlError(e: PdlException): ResponseEntity<ErrorMessage> {
//        log.error("Noe feilet ved kall til Pdl", e)
        val error = ErrorMessage(PDL_ERROR, "Noe uventet feilet")
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

}

data class ErrorMessage(
        val type: String?,
        val message: String?
)