package no.nav.sosialhjelp.modia.digisossak.fiks

import no.nav.sosialhjelp.api.fiks.DigisosSak

interface FiksClient {
    fun hentDigisosSak(digisosId: String): DigisosSak

    fun hentAlleDigisosSaker(fnr: String): List<DigisosSak>

    fun <T : Any> hentDokument(
        fnr: String,
        digisosId: String,
        dokumentlagerId: String,
        requestedClass: Class<out T>,
        cacheKey: String? = null,
    ): T
}
