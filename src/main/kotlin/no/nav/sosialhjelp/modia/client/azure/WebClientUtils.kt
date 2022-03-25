package no.nav.sosialhjelp.modia.client.azure

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

fun applicationJsonHttpHeaders(): HttpHeaders {
    val headers = HttpHeaders()
    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    return headers
}

fun applicationFormUrlencodedHeaders(): HttpHeaders {
    val headers = HttpHeaders()
    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    return headers
}
