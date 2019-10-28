package no.nav.sbl.sosialhjelpmodiaapi.abac

data class ResponseDTO(
        val decision: String
)

data class Advice(
        val id: String,
        val attributeAssignment: List<Attribute>
)

data class Response(
        val decision: Decision,
        val associatedAdvice: Advice
)

data class XacmlResponse(
        val response: Response
)

enum class Decision {
    Permit,
    Deny,
    NotApplicable,
    Indeterminate
}
