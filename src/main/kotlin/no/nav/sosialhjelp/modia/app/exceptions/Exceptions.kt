package no.nav.sosialhjelp.modia.app.exceptions

class NorgException(
    override val message: String?,
    override val cause: Throwable?,
) : RuntimeException(message, cause)

class ManglendeTilgangException(
    override val message: String?,
) : RuntimeException(message)

class ManglendeModiaSosialhjelpTilgangException(
    override val message: String?,
) : RuntimeException(message)

class PdlException(
    override val message: String?,
) : RuntimeException(message)

class DigisosSakTilhorerAnnenBrukerException(
    override val message: String?,
) : RuntimeException(message)

class MsGraphException(
    override val message: String?,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)
