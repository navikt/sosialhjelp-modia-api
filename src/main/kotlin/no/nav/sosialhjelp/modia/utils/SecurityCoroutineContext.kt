package no.nav.sosialhjelp.modia.utils

import kotlinx.coroutines.ThreadContextElement
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine context element that propagates Spring Security's SecurityContext
 * across coroutine boundaries.
 *
 * Similar to [kotlinx.coroutines.slf4j.MDCContext] but for Spring Security context.
 *
 * Usage:
 * ```
 * runBlocking(Dispatchers.IO + SecurityCoroutineContext()) {
 *     // SecurityContext is available here
 * }
 * ```
 */
class SecurityCoroutineContext(
    private val securityContext: SecurityContext = SecurityContextHolder.getContext(),
) : AbstractCoroutineContextElement(Key),
    ThreadContextElement<SecurityContext?> {
    companion object Key : CoroutineContext.Key<SecurityCoroutineContext>

    override fun updateThreadContext(context: CoroutineContext): SecurityContext? {
        val previous = SecurityContextHolder.getContext()
        SecurityContextHolder.setContext(securityContext)
        return previous
    }

    override fun restoreThreadContext(
        context: CoroutineContext,
        oldState: SecurityContext?,
    ) {
        if (oldState == null) {
            SecurityContextHolder.clearContext()
        } else {
            SecurityContextHolder.setContext(oldState)
        }
    }
}
