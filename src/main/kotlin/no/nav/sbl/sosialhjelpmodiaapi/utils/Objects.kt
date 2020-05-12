package no.nav.sbl.sosialhjelpmodiaapi.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.client.HttpClient
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(HttpClient::class.java)

val objectMapper: ObjectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()
        .registerModules(JavaTimeModule(), KotlinModule())
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
