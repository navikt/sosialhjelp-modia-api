package no.nav.sbl.sosialhjelpmodiaapi.abac

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object XacmlMapper {

    private val objectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

//    @SneakyThrows // try catch IOException
    fun mapRawResponse(content: String): XacmlResponse {
        return objectMapper.readValue(content, XacmlResponse::class.java)
    }

    fun mapRequestToEntity(request: XacmlRequest): String {
        return objectMapper.writeValueAsString(request)
    }
}
