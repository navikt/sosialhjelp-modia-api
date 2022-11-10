package no.nav.sosialhjelp.modia.app.mdc

import no.nav.sosialhjelp.modia.app.mdc.MDCUtils.clearMDC
import no.nav.sosialhjelp.modia.app.mdc.MDCUtils.generateCallId
import no.nav.sosialhjelp.modia.app.mdc.MDCUtils.put
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_CALL_ID
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class MDCFilter : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        addCallId(request)
        addDigisosId(request)
        put(MDCUtils.PATH, request.requestURI)

        try {
            filterChain.doFilter(request, response)
        } finally {
            clearMDC()
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return HttpMethod.valueOf(request.method) == HttpMethod.OPTIONS
    }

    private fun addCallId(request: HttpServletRequest) {
        request.getHeader(HEADER_CALL_ID)
            ?.let { put(MDCUtils.CALL_ID, it) }
            ?: put(MDCUtils.CALL_ID, generateCallId())
    }

    private fun addDigisosId(request: HttpServletRequest) {
        if (request.requestURI.matches(Regex("^$MODIA_BASE_URL(.*)/(personinfo|noekkelinfo|soknadDetaljer|saksStatus|oppgaver|dokumentasjonkrav|vilkar|hendelser|vedlegg|utbetalinger)"))) {
            val digisosId = request.requestURI.substringAfter(MODIA_BASE_URL).substringBefore("/")
            put(MDCUtils.DIGISOS_ID, digisosId)
        }
    }

    companion object {
        private const val MODIA_BASE_URL = "/sosialhjelp/modia-api/api/"
    }
}
