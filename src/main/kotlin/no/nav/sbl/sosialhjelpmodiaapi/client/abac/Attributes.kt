package no.nav.sbl.sosialhjelpmodiaapi.client.abac

import com.fasterxml.jackson.annotation.JsonProperty

data class Attributes(
        @JsonProperty("Attribute")
        var attributes: MutableList<Attribute>
)

data class Attribute(
        @JsonProperty("AttributeId")
        val attributeId: String,
        @JsonProperty("Value")
        val value: String
)