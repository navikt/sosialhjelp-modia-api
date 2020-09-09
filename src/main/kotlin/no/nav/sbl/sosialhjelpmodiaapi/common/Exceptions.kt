package no.nav.sbl.sosialhjelpmodiaapi.common


class FiksException(
        override val message: String?,
        override val cause: Throwable?
) : RuntimeException(message, cause)

class FiksNotFoundException(
        override val message: String?,
        override val cause: Throwable?,
        val digisosId: String
) : RuntimeException(message, cause)

class NorgException(
        override val message: String?,
        override val cause: Throwable?
) : RuntimeException(message, cause)

class AbacException(
        override val message: String?,
        override val cause: Throwable? = null
) : RuntimeException(message, cause)

class ManglendeTilgangException(
        override val message: String?
) : RuntimeException(message)

class PdlException(
        override val message: String?
) : RuntimeException(message)

class DigisosSakTilhorerAnnenBrukerException(
        override val message: String?
) : RuntimeException(message)