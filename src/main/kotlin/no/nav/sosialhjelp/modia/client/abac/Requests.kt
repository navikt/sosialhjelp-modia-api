package no.nav.sosialhjelp.modia.client.abac

import com.fasterxml.jackson.annotation.JsonProperty

data class XacmlRequest(
    @JsonProperty("Request")
    val request: Request
)

data class Request(
    @JsonProperty("Environment")
    val environment: Attributes?,
    @JsonProperty("Action")
    val action: Attributes?,
    @JsonProperty("Resource")
    val resource: Attributes?,
    @JsonProperty("AccessSubject")
    val accessSubject: Attributes?
)
