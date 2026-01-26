package no.nav.sosialhjelp.modia.digisossak.event

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.modia.digisossak.fiks.FiksClient
import org.springframework.stereotype.Component

@Component
class JsonDigisosSokerService(
    private val fiksClient: FiksClient,
) {
    suspend fun get(
        fnr: String,
        digisosId: String,
        digisosSokerMetadata: String?,
        timestampSistOppdatert: Long?,
    ): JsonDigisosSoker? =
        if (digisosSokerMetadata != null && timestampSistOppdatert != null) {
            fiksClient.hentDokument(
                fnr,
                digisosId,
                dokumentlagerId = digisosSokerMetadata,
                requestedClass = JsonDigisosSoker::class.java,
                cacheKey = "${digisosSokerMetadata}_$timestampSistOppdatert",
            )
        } else {
            null
        }
}
