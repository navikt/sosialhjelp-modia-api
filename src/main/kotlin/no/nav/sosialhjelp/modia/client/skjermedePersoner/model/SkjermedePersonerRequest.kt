package no.nav.sosialhjelp.modia.client.skjermedePersoner.model

import com.fasterxml.jackson.annotation.JsonProperty

data class SkjermedePersonerRequest(
    @JsonProperty("personident") val personIdent: String
)
