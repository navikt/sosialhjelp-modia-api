package no.nav.sosialhjelp.modia.utils

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ThreadContextElement
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

/**
 * Coroutine context element that propagates Spring's RequestContextHolder
 * across coroutine boundaries.
 *
 * Similar to [SecurityCoroutineContext] but for request attributes (e.g., cookies, session).
 *
 * Usage:
 * ```
 * runBlocking(Dispatchers.IO + RequestCoroutineContext()) {
 *     // RequestContextHolder is available here
 * }
 * ```
 */
class RequestCoroutineContext(
    private val requestAttributes: RequestAttributes? = RequestContextHolder.getRequestAttributes(),
) : ThreadContextElement<RequestAttributes?>, AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<RequestCoroutineContext>

    override fun updateThreadContext(context: CoroutineContext): RequestAttributes? {
        val previous = RequestContextHolder.getRequestAttributes()
        if (requestAttributes != null) {
            RequestContextHolder.setRequestAttributes(requestAttributes, true)
        } else {
            RequestContextHolder.resetRequestAttributes()
        }
        return previous
    }

    override fun restoreThreadContext(
        context: CoroutineContext,
        oldState: RequestAttributes?,
    ) {
        if (oldState == null) {
            RequestContextHolder.resetRequestAttributes()
        } else {
            RequestContextHolder.setRequestAttributes(oldState, true)
        }
    }
}
