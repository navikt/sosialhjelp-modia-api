package no.nav.sbl.sosialhjelpmodiaapi.abac

import com.fasterxml.jackson.annotation.JsonProperty

data class Advice(
        @JsonProperty("Id")
        val id: String,
        @JsonProperty("AttributeAssignment")
        val attributeAssignment: List<Attribute>
)

data class Response(
        @JsonProperty("Decision")
        val decision: Decision,
        @JsonProperty("AssociatedAdvice")
        val associatedAdvice: Advice?
)

data class XacmlResponse(
        @JsonProperty("Response")
        val response: Response
)

enum class Decision {
    Permit,
    Deny,
    NotApplicable,
    Indeterminate
}
