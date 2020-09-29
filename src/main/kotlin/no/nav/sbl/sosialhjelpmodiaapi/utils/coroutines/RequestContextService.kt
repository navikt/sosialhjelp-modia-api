package no.nav.sbl.sosialhjelpmodiaapi.utils.coroutines

import kotlin.coroutines.CoroutineContext

interface RequestContextService {

    fun getCoroutineContext(
            context: CoroutineContext,
//            userId: String,
//            callId: String
    ): CoroutineContext

//    fun getUserId(): String
//    fun getCallId(): String
    fun getRequestContext(): RequestContextServiceImpl.RequestContext
}