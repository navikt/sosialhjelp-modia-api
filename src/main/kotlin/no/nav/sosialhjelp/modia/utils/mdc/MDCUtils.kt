package no.nav.sosialhjelp.modia.utils.mdc

import org.slf4j.MDC
import java.security.SecureRandom

object MDCUtils {

    private const val CALL_ID = "callId"

    private val RANDOM = SecureRandom()

    fun getCallId(): String? {
        return MDC.get(CALL_ID)
    }

    fun setCallId(callId: String) {
        MDC.put(CALL_ID, callId)
    }

    fun clearCallId() {
        MDC.remove(CALL_ID)
    }

    fun generateCallId(): String {
        val randomNr = getRandomNumber()
        val systemTime = getSystemTime()

        return "CallId_${systemTime}_${randomNr}"
    }

    private fun getRandomNumber(): Int {
        return RANDOM.nextInt(Integer.MAX_VALUE)
    }

    private fun getSystemTime(): Long {
        return System.currentTimeMillis()
    }
}