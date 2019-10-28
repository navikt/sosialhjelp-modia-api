package no.nav.sbl.sosialhjelpmodiaapi.abac

data class Attributes(
        var attributes: MutableList<Attribute>
)

data class Attribute(
        val attributeId: String,
        val value: String
)