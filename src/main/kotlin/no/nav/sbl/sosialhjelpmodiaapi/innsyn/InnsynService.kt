package no.nav.sbl.sosialhjelpmodiaapi.innsyn

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import org.springframework.stereotype.Component

@Component
class InnsynService(private val fiksClient: FiksClient) {

    fun hentJsonDigisosSoker(digisosId: String, digisosSokerMetadata: String?, token: String): JsonDigisosSoker? {
        return when {
            digisosSokerMetadata != null -> fiksClient.hentDokument(digisosId, digisosSokerMetadata, JsonDigisosSoker::class.java, token) as JsonDigisosSoker
            else -> null
        }
    }

    fun hentOriginalSoknad(digisosId: String, originalSoknadNAVMetadata: String?, token: String): JsonSoknad? {
        return when {
            originalSoknadNAVMetadata != null -> fiksClient.hentDokument(digisosId, originalSoknadNAVMetadata, JsonSoknad::class.java, token) as JsonSoknad
            else -> null
        }
    }
}