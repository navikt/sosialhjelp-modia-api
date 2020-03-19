package no.nav.sbl.sosialhjelpmodiaapi.abac

import com.fasterxml.jackson.annotation.JsonProperty

data class XacmlResponse(
        @JsonProperty("Response")
        val response: List<Response>
)

data class Response(
        @JsonProperty("Decision")
        val decision: Decision,
        @JsonProperty("AssociatedAdvice")
        val associatedAdvice: Advice?
)

data class Advice(
        @JsonProperty("Id")
        val id: String,
        @JsonProperty("AttributeAssignment")
        val attributeAssignment: List<Attribute>
)

enum class Decision {
    Permit,
    Deny,
    NotApplicable,
    Indeterminate
}
