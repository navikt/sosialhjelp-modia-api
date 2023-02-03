package no.nav.sosialhjelp.modia.app.metrics

import io.micrometer.common.KeyValues
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequestObservationContext
import org.springframework.web.reactive.function.client.DefaultClientRequestObservationConvention

@Component
class ExtendedClientRequestObservationConvention : DefaultClientRequestObservationConvention() {

    override fun getLowCardinalityKeyValues(context: ClientRequestObservationContext): KeyValues {
        return super.getLowCardinalityKeyValues(context).and(clientName(context))
    }
}
