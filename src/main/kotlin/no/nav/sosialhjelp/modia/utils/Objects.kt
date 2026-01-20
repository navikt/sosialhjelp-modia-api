package no.nav.sosialhjelp.modia.utils

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

val sosialhjelpJsonMapper: JsonMapper =
    JsonSosialhjelpObjectMapper
        .createJsonMapperBuilder()
        .addModule(kotlinModule())
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .build()