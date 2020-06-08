package no.nav.sbl.sosialhjelpmodiaapi.service.innsyn

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import org.springframework.stereotype.Component

@Component
class InnsynService(
        private val fiksClient: FiksClient
) {

    fun hentJsonDigisosSoker(digisosId: String, digisosSokerMetadata: String?): JsonDigisosSoker? {
        return when {
            digisosSokerMetadata != null -> fiksClient.hentDokument(digisosId, digisosSokerMetadata, JsonDigisosSoker::class.java) as JsonDigisosSoker
            else -> null
        }
    }

    fun hentOriginalSoknad(digisosId: String, originalSoknadNAVMetadata: String?): JsonSoknad? {
        return when {
            originalSoknadNAVMetadata != null -> fiksClient.hentDokument(digisosId, originalSoknadNAVMetadata, JsonSoknad::class.java) as JsonSoknad
            else -> null
        }
    }
}