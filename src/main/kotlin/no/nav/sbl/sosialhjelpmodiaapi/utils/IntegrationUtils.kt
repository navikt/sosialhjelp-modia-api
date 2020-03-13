package no.nav.sbl.sosialhjelpmodiaapi.utils

import java.security.SecureRandom

private val RANDOM = SecureRandom()

fun generateCallId(): String {
    val randomNr = getRandomNumber()
    val systemTime = getSystemTime()

    return String.format("CallId_%s_%s", systemTime, randomNr)
}

private fun getRandomNumber(): Int {
    return RANDOM.nextInt(Integer.MAX_VALUE)
}

private fun getSystemTime(): Long {
    return System.currentTimeMillis()
}

object IntegrationUtils {
    const val HEADER_INTEGRASJON_ID = "IntegrasjonId"
    const val HEADER_INTEGRASJON_PASSORD = "IntegrasjonPassord"

    const val KILDE_INNSYN_API = "innsyn-api"

    const val BEARER = "Bearer "

    const val TEMA_KOM = "KOM"

    const val NAV_CALL_ID = "Nav-Call-Id"
    const val NAV_CONSUMER_TOKEN = "Nav-Consumer-Token"
    const val TEMA = "Tema"
}
