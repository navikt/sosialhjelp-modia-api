package no.nav.sosialhjelp.modia.tilgang.skjermedepersoner.model

import com.fasterxml.jackson.annotation.JsonProperty

data class SkjermedePersonerRequest(
    @param:JsonProperty("personident") val personIdent: String,
)
