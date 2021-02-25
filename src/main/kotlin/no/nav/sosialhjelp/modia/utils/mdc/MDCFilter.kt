package no.nav.sosialhjelp.modia.utils.mdc

import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_CALL_ID
import no.nav.sosialhjelp.modia.utils.mdc.MDCUtils.clearCallId
import no.nav.sosialhjelp.modia.utils.mdc.MDCUtils.generateCallId
import no.nav.sosialhjelp.modia.utils.mdc.MDCUtils.setCallId
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class MDCFilter : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        request.getHeader(HEADER_CALL_ID)
                ?.let { setCallId(it) }
                ?: setCallId(generateCallId())

        try {
            filterChain.doFilter(request, response)
        } finally {
            clearCallId()
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return HttpMethod.valueOf(request.method) == HttpMethod.OPTIONS
    }

}