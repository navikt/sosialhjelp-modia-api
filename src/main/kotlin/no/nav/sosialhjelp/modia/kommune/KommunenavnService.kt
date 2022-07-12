package no.nav.sosialhjelp.modia.kommune

import no.nav.sosialhjelp.modia.kommune.kartverket.KommunenavnClient
import no.nav.sosialhjelp.modia.logger
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class KommunenavnService(
    private val kommunenavnClient: KommunenavnClient
) {
    private var kommunenavnMap: Map<String, String> = HashMap()
    private var sistOppdatert = LocalDateTime.now().minusDays(2)

    fun hentKommunenavnFor(kommunenummer: String): String {
        if (sistOppdatert.isBefore(LocalDateTime.now().minusDays(1))) {
            oppdaterKommunenavnMap()
        }
        return kommunenavnMap[kommunenummer] ?: "[Kan ikke hente kommune for kommunenummer \"$kommunenummer\"]"
    }

    private fun oppdaterKommunenavnMap() {
        try {
            val svar = kommunenavnClient.getAll()
            val nyMap = svar.containeditems.associateBy({ it.codevalue }, { it.description })
            kommunenavnMap = nyMap
            sistOppdatert = LocalDateTime.now()
        } catch (e: Exception) {
            log.warn("Feil under oppdatering av kommunenavnMap", e)
        }
    }

    companion object {
        private val log by logger()
    }
}
