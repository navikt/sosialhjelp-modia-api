package no.nav.sosialhjelp.modia.app.mdc

import org.slf4j.MDC
import java.security.SecureRandom

object MDCUtils {
    const val CALL_ID = "callId"
    const val DIGISOS_ID = "digisosId"
    const val PATH = "path"

    private val RANDOM = SecureRandom()

    fun get(key: String): String? = MDC.get(key)

    fun put(
        key: String,
        value: String,
    ) {
        MDC.put(key, value)
    }

    fun getCallId(): String? = MDC.get(CALL_ID)

    fun clearMDC() {
        MDC.remove(CALL_ID)
        MDC.remove(DIGISOS_ID)
        MDC.remove(PATH)
    }

    fun generateCallId(): String {
        val randomNr = getRandomNumber()
        val systemTime = getSystemTime()

        return "CallId_${systemTime}_$randomNr"
    }

    private fun getRandomNumber(): Int = RANDOM.nextInt(Integer.MAX_VALUE)

    private fun getSystemTime(): Long = System.currentTimeMillis()
}
