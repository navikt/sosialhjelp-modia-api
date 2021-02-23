package no.nav.sbl.sosialhjelpmodiaapi.client.abac

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.abac.xacml.NavAttributter.ADVICEOROBLIGATION_DENY_POLICY
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacConstants.FP1_KODE6
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacConstants.FP2_KODE7
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacConstants.FP3_EGEN_ANSATT
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacConstants.SOSIALHJELP_AD_ROLLE

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

fun Attribute.manglerTilgangSosialhjelp(): Boolean {
    return attributeId == ADVICEOROBLIGATION_DENY_POLICY && value == SOSIALHJELP_AD_ROLLE
}

fun Attribute.manglerTilgangKode6Kode7EllerEgenAnsatt(): Boolean {
    return attributeId == ADVICEOROBLIGATION_DENY_POLICY && (value == FP1_KODE6 || value == FP2_KODE7 || value == FP3_EGEN_ANSATT)
}
