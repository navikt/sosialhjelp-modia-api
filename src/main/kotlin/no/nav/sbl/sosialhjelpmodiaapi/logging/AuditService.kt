package no.nav.sbl.sosialhjelpmodiaapi.logging

import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacResponse
import no.nav.sbl.sosialhjelpmodiaapi.subjecthandler.SubjectHandlerUtils.getConsumerId
import no.nav.sbl.sosialhjelpmodiaapi.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sbl.sosialhjelpmodiaapi.utils.mdc.MDCUtils
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Component
class AuditService(
        private val auditLogger: AuditLogger
) {

    private fun commonAttributes(brukerFnr: String, url: String, httpMethod: HttpMethod): Map<String, Any> {
        return mutableMapOf(
                CALL_ID to (MDCUtils.getCallId() ?: ""),
                CONSUMER_ID to getConsumerId(),
                NAVIDENT to getUserIdFromToken(),
                BRUKER_FNR to brukerFnr,
                URL to url,
                HTTP_METHOD to httpMethod
        )
    }

    fun reportAbac(brukerFnr: String, url: String, httpMethod: HttpMethod, abacResponse: AbacResponse) {
        val attributes: Map<String, Any> = commonAttributes(brukerFnr, url, httpMethod)
                .plus(
                        mapOf(
                                TITLE to "Abac",
                                RESOURCE to RESOURCE_ABAC_ACCESS,
                                ABAC_RESPONSE to abacResponse
                        )
                )
        auditLogger.report(attributes)
    }

    fun reportFiks(brukerFnr: String, url: String, httpMethod: HttpMethod, fiksRequestId: String) {
        val attributes: Map<String, Any> = commonAttributes(brukerFnr, url, httpMethod)
                .plus(
                        mapOf(
                                TITLE to "Fiks audit",
                                RESOURCE to RESOURCE_FIKS_AUDIT,
                                FIKS_REQUEST_ID to fiksRequestId
                        )
                )
        auditLogger.report(attributes)
    }
}