package no.nav.sosialhjelp.modia.utils

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

fun sosialhjelpJsonMapperBuilder(): JsonMapper.Builder =
    JsonSosialhjelpObjectMapper
        .createJsonMapperBuilder()
        .addModule(kotlinModule())
        .configure(SerializationFeature.INDENT_OUTPUT, true)

val sosialhjelpJsonMapper: JsonMapper = sosialhjelpJsonMapperBuilder().build()
