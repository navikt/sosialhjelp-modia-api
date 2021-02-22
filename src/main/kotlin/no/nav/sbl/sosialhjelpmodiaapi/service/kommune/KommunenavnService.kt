package no.nav.sbl.sosialhjelpmodiaapi.service.kommune

import no.nav.sbl.sosialhjelpmodiaapi.client.kommunenavn.KommunenavnClient
import no.nav.sbl.sosialhjelpmodiaapi.logger
import org.joda.time.DateTime
import org.springframework.stereotype.Service

@Service
class KommunenavnService(
    private val kommunenavnClient: KommunenavnClient
) {
    private var kommunenavnMap: Map<String, String> = HashMap()
    private var sistOppdatert = DateTime.now().minusDays(2)

    fun hentKommunenavnFor(kommunenummer: String): String {
        if (sistOppdatert.isBefore(DateTime.now().minusDays(1))) {
            oppdaterKommunenavnMap()
        }
        return kommunenavnMap[kommunenummer] ?: "[Kan ikke hente kommune]"
    }

    private fun oppdaterKommunenavnMap() {
        try {
            val svar = kommunenavnClient.getAll()
            val nyMap = svar.containeditems.associateBy({ it.codevalue }, { it.description })
            kommunenavnMap = nyMap
            sistOppdatert = DateTime.now()
        } catch (e: Exception) {
            log.warn("Feil under oppdatering av kommunenavnMap", e)
        }
    }

    companion object {
        private val log by logger()
    }
}
