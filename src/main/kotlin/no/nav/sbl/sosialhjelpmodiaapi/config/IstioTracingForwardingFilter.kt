package no.nav.sbl.sosialhjelpmodiaapi.config
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.X_B3_FLAGS
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.X_B3_PARENTSPANID
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.X_B3_SAMPLED
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.X_B3_SPANID
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.X_B3_TRACEID
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.X_OT_SPAN_CONTEXT
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.X_REQUEST_ID
import org.slf4j.MDC
import org.springframework.stereotype.Component
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

@Component
class IstioTracingForwardingFilter : Filter {
    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        val httpRequest = request as HttpServletRequest
        httpRequest.getHeader(X_REQUEST_ID)?.let { MDC.put(X_REQUEST_ID, it) }
        httpRequest.getHeader(X_B3_TRACEID)?.let { MDC.put(X_B3_TRACEID, it) }
        httpRequest.getHeader(X_B3_SPANID)?.let { MDC.put(X_B3_SPANID, it) }
        httpRequest.getHeader(X_B3_PARENTSPANID)?.let { MDC.put(X_B3_PARENTSPANID, it) }
        httpRequest.getHeader(X_B3_SAMPLED)?.let { MDC.put(X_B3_SAMPLED, it) }
        httpRequest.getHeader(X_B3_FLAGS)?.let { MDC.put(X_B3_FLAGS, it) }
        httpRequest.getHeader(X_OT_SPAN_CONTEXT)?.let { MDC.put(X_OT_SPAN_CONTEXT, it) }

        try {
            chain?.doFilter(request, response)
        } finally {
            MDC.remove(X_REQUEST_ID)
            MDC.remove(X_B3_TRACEID)
            MDC.remove(X_B3_SPANID)
            MDC.remove(X_B3_PARENTSPANID)
            MDC.remove(X_B3_SAMPLED)
            MDC.remove(X_B3_FLAGS)
            MDC.remove(X_OT_SPAN_CONTEXT)
            MDC.remove("PCN_Test")
        }
    }
}