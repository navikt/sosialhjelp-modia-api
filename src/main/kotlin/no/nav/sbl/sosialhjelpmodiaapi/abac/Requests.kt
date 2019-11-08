package no.nav.sbl.sosialhjelpmodiaapi.abac

data class Request(
        val environment: Attributes?,
        val action: Attributes?,
        val resource: Attributes?,
        val accessObject: Attributes?
)

data class XacmlRequest(
        val request: Request
)