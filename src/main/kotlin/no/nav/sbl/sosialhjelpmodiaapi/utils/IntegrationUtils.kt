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
}