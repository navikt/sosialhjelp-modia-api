package no.nav.sosialhjelp.modia.app.metrics

import io.micrometer.common.KeyValues
import org.springframework.http.client.observation.ClientRequestObservationContext
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention
import org.springframework.stereotype.Component

@Component
class ExtendedClientRequestObservationConvention : DefaultClientRequestObservationConvention() {

    override fun getLowCardinalityKeyValues(context: ClientRequestObservationContext): KeyValues {
        return super.getLowCardinalityKeyValues(context).and(clientName(context))
    }
}
