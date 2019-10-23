package no.nav.sbl.sosialhjelpmodiaapi.common

import io.ktor.features.origin
import io.ktor.request.ApplicationRequest

internal sealed class ParamType(private val description: String) {
    object Header : ParamType("header")
    object Parameter : ParamType("parameter")
    object QueryParameter : ParamType("query parameter")

    override fun toString(): String = this.description
}

internal fun ApplicationRequest.url(): String {
    val port = when (origin.port) {
        in listOf(80, 443) -> ""
        else -> ":${origin.port}"
    }
    return "${origin.scheme}://${origin.host}$port${origin.uri}"
}

