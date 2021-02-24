package no.nav.sosialhjelp.modia.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper


val objectMapper: ObjectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()
        .registerModules(JavaTimeModule(), KotlinModule())
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
