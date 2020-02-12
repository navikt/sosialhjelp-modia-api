package no.nav.sbl.sosialhjelpmodiaapi.utils

import java.security.SecureRandom
import java.util.*

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
}

fun basicHeader(credentialUsername: String, credentialPassword: String): String {
    return "Basic " + Base64.getEncoder().encodeToString("$credentialUsername:$credentialPassword".toByteArray())
}
