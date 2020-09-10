package no.nav.sbl.sosialhjelpmodiaapi.config;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.X_REQUEST_ID;
import static no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.X_B3_TRACEID;
import static no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.X_B3_SPANID;
import static no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.X_B3_PARENTSPANID;
import static no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.X_B3_SAMPLED;
import static no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.X_B3_FLAGS;
import static no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.X_OT_SPAN_CONTEXT;

@Component
public class HeaderForwardingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        MDC.put(X_REQUEST_ID, httpRequest.getHeader(X_REQUEST_ID));
        MDC.put(X_B3_TRACEID, httpRequest.getHeader(X_B3_TRACEID));
        MDC.put(X_B3_SPANID, httpRequest.getHeader(X_B3_SPANID));
        MDC.put(X_B3_PARENTSPANID, httpRequest.getHeader(X_B3_PARENTSPANID));
        MDC.put(X_B3_SAMPLED, httpRequest.getHeader(X_B3_SAMPLED));
        MDC.put(X_B3_FLAGS, httpRequest.getHeader(X_B3_FLAGS));
        MDC.put(X_OT_SPAN_CONTEXT, httpRequest.getHeader(X_OT_SPAN_CONTEXT));
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
