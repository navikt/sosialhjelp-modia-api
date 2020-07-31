package no.nav.sbl.sosialhjelpmodiaapi.utils

import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
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

    fun fiksHeaders(clientProperties: ClientProperties, token: String): HttpHeaders {
        val headers = defaultFiksHeaders(clientProperties)
        headers.set(HttpHeaders.AUTHORIZATION, token)
        return headers
    }

    private fun defaultFiksHeaders(clientProperties: ClientProperties): HttpHeaders {
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.set(HEADER_INTEGRASJON_ID, clientProperties.fiksIntegrasjonId)
        headers.set(HEADER_INTEGRASJON_PASSORD, clientProperties.fiksIntegrasjonpassord)
        return headers
    }
}
