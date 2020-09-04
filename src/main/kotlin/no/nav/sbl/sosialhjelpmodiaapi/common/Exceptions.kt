package no.nav.sbl.sosialhjelpmodiaapi.common

import org.springframework.http.HttpStatus

class FiksException(
        status: HttpStatus?,
        override val message: String?,
        override val cause: Throwable?
) : RuntimeException(message, cause)

class FiksNotFoundException(
        status: HttpStatus?,
        override val message: String?,
        override val cause: Throwable?,
        val digisosId: String
) : RuntimeException(message, cause)

class NorgException(
        status: HttpStatus?,
        override val message: String?,
        override val cause: Throwable?
) : RuntimeException(message, cause)

class TilgangskontrollException(
        override val message: String?,
        override val cause: Throwable? = null
): RuntimeException(message, cause)

class PdlException(
        status: HttpStatus?,
        override val message: String?
) : RuntimeException(message)

class DigisosSakTilhorerAnnenBrukerException(
        override val message: String?
) : RuntimeException(message)