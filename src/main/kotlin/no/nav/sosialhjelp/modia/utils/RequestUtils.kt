package no.nav.sosialhjelp.modia.utils

import org.springframework.web.context.request.RequestContextHolder.getRequestAttributes
import org.springframework.web.context.request.ServletRequestAttributes

object RequestUtils {

    private const val SOSIALHJELP_MODIA_COOKIE_NAME = "sosialhjelp-modia"

    fun getSosialhjelpModiaSessionId(): String? {
        val requestAttributes: ServletRequestAttributes? = getRequestAttributes() as? ServletRequestAttributes
        return requestAttributes?.request?.cookies
            ?.firstOrNull { it.name == SOSIALHJELP_MODIA_COOKIE_NAME }?.value
    }
}
