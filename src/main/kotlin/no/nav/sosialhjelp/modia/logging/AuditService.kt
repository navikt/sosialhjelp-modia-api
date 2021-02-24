package no.nav.sosialhjelp.modia.logging

import no.nav.sosialhjelp.modia.client.abac.AbacResponse
import no.nav.sosialhjelp.modia.utils.Miljo.SRVSOSIALHJELP_MOD
import no.nav.sosialhjelp.modia.utils.TokenUtils
import no.nav.sosialhjelp.modia.utils.mdc.MDCUtils
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Component
class AuditService(
        private val auditLogger: AuditLogger,
        private val tokenUtils: TokenUtils
) {

    private fun commonAttributes(brukerFnr: String, url: String, httpMethod: HttpMethod): Map<String, Any> {
        return mutableMapOf(
                CALL_ID to (MDCUtils.getCallId() ?: ""),
                CONSUMER_ID to SRVSOSIALHJELP_MOD,
                NAVIDENT to tokenUtils.hentNavIdentForInnloggetBruker(),
                BRUKER_FNR to brukerFnr,
                URL to url,
                HTTP_METHOD to httpMethod
        )
    }

    fun reportAbac(brukerFnr: String, url: String, httpMethod: HttpMethod, abacResponse: AbacResponse) {
        val attributes: Map<String, Any> = commonAttributes(brukerFnr, url, httpMethod)
                .plus(
                        mapOf(
                                TITLE to TITLE_ABAC,
                                RESOURCE to RESOURCE_AUDIT_ACCESS,
                                ABAC_RESPONSE to abacResponse
                        )
                )
        auditLogger.report(attributes)
    }

    fun reportFiks(brukerFnr: String, url: String, httpMethod: HttpMethod, fiksRequestId: String) {
        val attributes: Map<String, Any> = commonAttributes(brukerFnr, url, httpMethod)
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