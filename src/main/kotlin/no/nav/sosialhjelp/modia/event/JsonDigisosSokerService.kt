package no.nav.sosialhjelp.modia.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.modia.client.fiks.FiksClient
import org.springframework.stereotype.Component

@Component
class JsonDigisosSokerService(
    private val fiksClient: FiksClient
) {
    fun get(fnr: String, digisosId: String, digisosSokerMetadata: String?): JsonDigisosSoker? {
        return when {
            digisosSokerMetadata != null -> fiksClient.hentDokument(fnr, digisosId, digisosSokerMetadata, JsonDigisosSoker::class.java) as JsonDigisosSoker
            else -> null
        }
    }
}
