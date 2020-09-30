package no.nav.sbl.sosialhjelpmodiaapi.utils.coroutines

import org.springframework.web.context.request.RequestAttributes
import kotlin.coroutines.CoroutineContext

interface RequestContextService {

    fun getCoroutineContext(
            context: CoroutineContext,
            requestAttributes: RequestAttributes?
    ): CoroutineContext
}