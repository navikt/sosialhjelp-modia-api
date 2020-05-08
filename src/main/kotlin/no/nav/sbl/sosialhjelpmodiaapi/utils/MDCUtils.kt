package no.nav.sbl.sosialhjelpmodiaapi.utils

import org.slf4j.MDC

object MDCUtils {

    private const val CALL_ID = "callId"

    fun getCallId(): String? {
        return MDC.get(CALL_ID)
    }

    fun setCallId(callId: String) {
        MDC.put(CALL_ID, callId)
    }

    fun clearCallId() {
        MDC.remove(CALL_ID)
    }

}