package no.nav.sosialhjelp.modia.logging

import no.nav.sosialhjelp.modia.app.mdc.MDCUtils
import no.nav.sosialhjelp.modia.utils.Miljo.SRVSOSIALHJELP_MOD
import no.nav.sosialhjelp.modia.utils.TokenUtils
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Component
class AuditService(
    private val auditLogger: AuditLogger,
    private val tokenUtils: TokenUtils
) {

    private fun commonAttributes(brukerFnr: String, url: String, httpMethod: HttpMethod, access: Access): Map<String, Any> {
        return mutableMapOf(
            CALL_ID to (MDCUtils.getCallId() ?: ""),
            CONSUMER_ID to SRVSOSIALHJELP_MOD,
            NAVIDENT to tokenUtils.hentNavIdentForInnloggetBruker(),
            BRUKER_FNR to brukerFnr,
            URL to url,
            HTTP_METHOD to httpMethod,
            ACCESS to access.name
        )
    }

    fun reportToAuditlog(brukerFnr: String, url: String, httpMethod: HttpMethod, access: Access = Access.DENY) {
        val attributes: Map<String, Any> = commonAttributes(brukerFnr, url, httpMethod, access)
            .plus(
                mapOf(
                    RESOURCE to RESOURCE_AUDIT_ACCESS
                )
            )
        auditLogger.report(attributes)
    }

    fun reportFiks(
        brukerFnr: String,
        url: String,
        httpMethod: HttpMethod,
        fiksRequestId: String,
        access: Access = Access.PERMIT
    ) {
        val attributes: Map<String, Any> = commonAttributes(brukerFnr, url, httpMethod, access)
            .plus(
                mapOf(
                    TITLE to TITLE_FIKS,
                    RESOURCE to RESOURCE_AUDIT_ACCESS,
                    FIKS_REQUEST_ID to fiksRequestId
                )
            )
        auditLogger.report(attributes)
    }
}
