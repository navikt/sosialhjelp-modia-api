package no.nav.sosialhjelp.modia.client.fiks

import no.nav.sosialhjelp.api.fiks.DigisosSak

interface FiksClient {

    fun hentDigisosSak(digisosId: String): DigisosSak

    fun hentAlleDigisosSaker(fnr: String): List<DigisosSak>

    fun hentDokument(fnr: String, digisosId: String, dokumentlagerId: String, requestedClass: Class<out Any>): Any
}
