package no.nav.sbl.sosialhjelpmodiaapi.client.abac

import com.fasterxml.jackson.annotation.JsonProperty

data class XacmlResponse(
    @JsonProperty("Response")
    val response: List<AbacResponse>
)

data class AbacResponse(
    @JsonProperty("Decision")
    val decision: Decision,
    @JsonProperty("AssociatedAdvice")
    val associatedAdvice: List<Advice>?
)

data class Advice(
    @JsonProperty("Id")
    val id: String,
    @JsonProperty("AttributeAssignment")
    val attributeAssignment: List<Attribute>?
)

enum class Decision {
    Permit,
    Deny,
    NotApplicable,
    Indeterminate
}
