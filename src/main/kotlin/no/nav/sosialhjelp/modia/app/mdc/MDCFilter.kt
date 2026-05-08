package no.nav.sosialhjelp.modia.app.mdc

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.modia.app.mdc.MDCUtils.clearMDC
import no.nav.sosialhjelp.modia.app.mdc.MDCUtils.putToMDC
import no.nav.sosialhjelp.modia.logger
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.server.CoWebFilter
import org.springframework.web.server.CoWebFilterChain
import org.springframework.web.server.ServerWebExchange
import java.util.*

@Component
class MDCFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        addDigisosId(request)
        putToMDC(MDCUtils.PATH, request.requestURI)
        putToMDC(MDCUtils.METHOD, request.method)

        try {
            filterChain.doFilter(request, response)
        } finally {
            clearMDC()
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean = request.method == HttpMethod.OPTIONS.name()

    private fun addDigisosId(request: HttpServletRequest) {
        val digisosIdOrNull =
            request.requestURI
                .substringAfter(MODIA_BASE_URL)
                .substringBefore("/")
                .let { runCatching { UUID.fromString(it) }.getOrNull() }
                ?: "not found"

        putToMDC(MDCUtils.DIGISOS_ID, digisosIdOrNull.toString())
    }

    companion object {
        private const val MODIA_BASE_URL = "/sosialhjelp/modia-api/api/"
    }
}

@Component
class MdcCoWebFilter: CoWebFilter() {
    override suspend fun filter(
        exchange: ServerWebExchange,
        chain: CoWebFilterChain
    ) {
        val request = exchange.request
        val digisosIdOrNull =
            request.uri.path
                .substringAfter(MODIA_BASE_URL)
                .substringBefore("/")
                .let { runCatching { UUID.fromString(it) }.getOrNull() }
                ?.toString()
                ?: "not found"

        putToMDC(MDCUtils.PATH, digisosIdOrNull)
    }


    companion object {
        private val logger by logger()
        private const val MODIA_BASE_URL = "/sosialhjelp/modia-api/api/"
    }
}
