package no.nav.sbl.sosialhjelpmodiaapi.utils.coroutines

import kotlinx.coroutines.asContextElement
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

private class CoroutineRequestContext(
        internal val callId: String,
        internal val userId: String
) : AbstractCoroutineContextElement(Key) {

    companion object Key: CoroutineContext.Key<CoroutineRequestContext>
}

private fun CoroutineContext.requestContext() = get(CoroutineRequestContext.Key) ?: throw IllegalStateException("Request Context har ikke blitt satt.")
internal fun CoroutineContext.userId() = requestContext().userId
internal fun CoroutineContext.callId() = requestContext().callId

//@Profile("!mock")
@Component
class RequestContextServiceImpl : RequestContextService {

    private companion object {
        private val requestContexts = ThreadLocal<RequestContext>()
    }

    override fun getCoroutineContext(
            context: CoroutineContext,
            userId: String,
            callId: String
    ) = context + requestContexts.asContextElement(
            RequestContext(
                    userId, callId
            )
    ) + CoroutineRequestContext(
            callId, userId
    )

    override fun getRequestContext() = requestContexts.get() ?: throw IllegalStateException("Request Context ikke satt.")

    override fun getUserId(): String = getRequestContext().userId
    override fun getCallId(): String = getRequestContext().callId

    data class RequestContext(
            val userId: String,
            val callId: String
    )
}

class RequestContextServiceMock : RequestContextService {
    private companion object {
        private val requestContexts = ThreadLocal<RequestContextServiceImpl.RequestContext>()
    }
    override fun getCoroutineContext(
            context: CoroutineContext,
            userId: String,
            callId: String
    ) = context + requestContexts.asContextElement(
            RequestContextServiceImpl.RequestContext(
                    userId, callId
            )
    ) + CoroutineRequestContext(
            callId, userId
    )


    override fun getUserId(): String {
        return "11111111111"
    }

    override fun getCallId(): String {
        TODO("Not yet implemented")
    }

    override fun getRequestContext(): RequestContextServiceImpl.RequestContext {
        TODO("Not yet implemented")
    }
}