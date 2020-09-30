package no.nav.sbl.sosialhjelpmodiaapi.utils.coroutines

import kotlinx.coroutines.asContextElement
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

private class CoroutineRequestContext(
        internal val requestAttributes: RequestAttributes?
) : AbstractCoroutineContextElement(Key) {

    internal companion object Key: CoroutineContext.Key<CoroutineRequestContext>
}

private fun CoroutineContext.requestContext() = get(CoroutineRequestContext.Key) ?: throw IllegalStateException("Request Context har ikke blitt satt.")

private fun setRequestAttributes(requestAttributes: RequestAttributes?) {
    requestAttributes?.let { RequestContextHolder.setRequestAttributes(it) }
}

@Component
class RequestContextServiceImpl : RequestContextService {

    private companion object {
        private val requestContexts = ThreadLocal<RequestContext>()
    }

    override fun getCoroutineContext(
            context: CoroutineContext,
            requestAttributes: RequestAttributes?
    ): CoroutineContext {

        setRequestAttributes(requestAttributes)

        return context + requestContexts.asContextElement(
                RequestContext(
                        requestAttributes
                )
        ) + CoroutineRequestContext(
                requestAttributes
        )
    }

    data class RequestContext(
            val requestAttributes: RequestAttributes?
    )
}

class RequestContextServiceMock : RequestContextService {
    private companion object {
        private val requestContexts = ThreadLocal<RequestContextServiceImpl.RequestContext>()
    }
    override fun getCoroutineContext(
            context: CoroutineContext,
            requestAttributes: RequestAttributes?
    ): CoroutineContext {

        setRequestAttributes(requestAttributes)

        return context + requestContexts.asContextElement(
                RequestContextServiceImpl.RequestContext(
                        requestAttributes
                )
        ) + CoroutineRequestContext(
                requestAttributes
        )
    }
}