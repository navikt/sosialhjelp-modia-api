package no.nav.sbl.sosialhjelpmodiaapi.utils.coroutines

import kotlinx.coroutines.GlobalScope
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import kotlin.coroutines.CoroutineContext

interface RequestContextService {

    fun getCoroutineContext(): CoroutineContext

}

@Component
class RequestContextServiceImpl : RequestContextService {

    override fun getCoroutineContext(): CoroutineContext {
        return getCoroutineContext(
                context = GlobalScope.coroutineContext,
                requestAttributes = RequestContextHolder.getRequestAttributes()
        )
    }

    private fun getCoroutineContext(
            context: CoroutineContext,
            requestAttributes: RequestAttributes?
    ): CoroutineContext {

        setRequestAttributes(requestAttributes)

        return context
    }

    private fun setRequestAttributes(requestAttributes: RequestAttributes?) {
        requestAttributes?.let { RequestContextHolder.setRequestAttributes(it) }
    }
}
