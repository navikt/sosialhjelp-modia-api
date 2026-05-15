package no.nav.sosialhjelp.modia.app.config

import io.micrometer.context.ContextRegistry
import org.slf4j.MDC
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Hooks

@Configuration
class MdcContextPropagationConfig {
    init {
        ContextRegistry.getInstance().registerThreadLocalAccessor(
            "mdc",
            { MDC.getCopyOfContextMap() ?: emptyMap<String, String>() },
            { map -> if (map != null) MDC.setContextMap(map) else MDC.clear() },
            { MDC.clear() },
        )
        Hooks.enableAutomaticContextPropagation()
    }
}
