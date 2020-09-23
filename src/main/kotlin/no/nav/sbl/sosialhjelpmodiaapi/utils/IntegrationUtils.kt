package no.nav.sbl.sosialhjelpmodiaapi.utils

import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

object IntegrationUtils {
    const val HEADER_INTEGRASJON_ID = "IntegrasjonId"
    const val HEADER_INTEGRASJON_PASSORD = "IntegrasjonPassord"

    const val KILDE_INNSYN_API = "innsyn-api"

    const val BEARER = "Bearer "

    const val HEADER_TEMA = "Tema"
    const val TEMA_KOM = "KOM"

    const val HEADER_CALL_ID = "Nav-Call-Id"
    const val HEADER_CONSUMER_TOKEN = "Nav-Consumer-Token"

    const val X_REQUEST_ID = "x-request-id"
    const val X_B3_TRACEID = "x-b3-traceid"
    const val X_B3_SPANID = "x-b3-spanid"
    const val X_B3_PARENTSPANID = "x-b3-parentspanid"
    const val X_B3_SAMPLED = "x-b3-sampled"
    const val X_B3_FLAGS = "x-b3-flags"
    const val X_OT_SPAN_CONTEXT = "x-ot-span-context"


    fun fiksHeaders(clientProperties: ClientProperties, token: String): HttpHeaders {
        val headers = defaultFiksHeaders(clientProperties)
        headers.set(HttpHeaders.AUTHORIZATION, token)
        return headers
    }

    fun forwardHeaders(headers: HttpHeaders = HttpHeaders()): HttpHeaders {
        headers.set(X_REQUEST_ID, MDC.get(X_REQUEST_ID))
        headers.set(X_B3_TRACEID, MDC.get(X_B3_TRACEID))
        headers.set(X_B3_SPANID, MDC.get(X_B3_SPANID))
        headers.set(X_B3_PARENTSPANID, MDC.get(X_B3_PARENTSPANID))
        headers.set(X_B3_SAMPLED, MDC.get(X_B3_SAMPLED))
        headers.set(X_B3_FLAGS, MDC.get(X_B3_FLAGS))
        headers.set(X_OT_SPAN_CONTEXT, MDC.get(X_OT_SPAN_CONTEXT))
        return headers
    }

    private fun defaultFiksHeaders(clientProperties: ClientProperties): HttpHeaders {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.set(HEADER_INTEGRASJON_ID, clientProperties.fiksIntegrasjonId)
        headers.set(HEADER_INTEGRASJON_PASSORD, clientProperties.fiksIntegrasjonpassord)
        forwardHeaders(headers)
        return headers
    }
}
