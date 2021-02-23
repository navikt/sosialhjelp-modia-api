package no.nav.sbl.sosialhjelpmodiaapi.service.innsyn

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import org.springframework.stereotype.Component

@Component
class InnsynService(
    private val fiksClient: FiksClient
) {

    fun hentJsonDigisosSoker(fnr: String, digisosId: String, digisosSokerMetadata: String?): JsonDigisosSoker? {
        return when {
            digisosSokerMetadata != null -> fiksClient.hentDokument(fnr, digisosId, digisosSokerMetadata, JsonDigisosSoker::class.java) as JsonDigisosSoker
            else -> null
        }
    }
}
