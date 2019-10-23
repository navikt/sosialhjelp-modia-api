package no.nav.sbl.sosialhjelpmodiaapi.common

import org.springframework.http.HttpStatus

class InvalidInputException(message: String) : Exception(message)

class FiksException(status: HttpStatus?, override val message: String?, override val cause: Throwable?): RuntimeException(message, cause)

class NorgException(status: HttpStatus?, override val message: String?, override val cause: Throwable?): RuntimeException(message,cause)

class OpplastingException(override val message: String?, override val cause: Throwable?): RuntimeException(message, cause)