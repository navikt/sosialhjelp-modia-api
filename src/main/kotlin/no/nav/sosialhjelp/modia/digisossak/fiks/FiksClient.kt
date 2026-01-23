package no.nav.sosialhjelp.modia.digisossak.fiks

import no.nav.sosialhjelp.api.fiks.DigisosSak

interface FiksClient {
    suspend fun hentDigisosSak(digisosId: String): DigisosSak

    suspend fun hentAlleDigisosSaker(fnr: String): List<DigisosSak>

    suspend fun <T : Any> hentDokument(
        fnr: String,
        digisosId: String,
        dokumentlagerId: String,
        requestedClass: Class<out T>,
        cacheKey: String? = null,
    ): T
}
