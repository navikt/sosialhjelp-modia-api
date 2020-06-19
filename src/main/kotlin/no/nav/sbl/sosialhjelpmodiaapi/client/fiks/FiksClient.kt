package no.nav.sbl.sosialhjelpmodiaapi.client.fiks

import no.nav.sosialhjelp.api.fiks.DigisosSak

interface FiksClient {

    fun hentDigisosSak(digisosId: String): DigisosSak

    fun hentAlleDigisosSaker(fnr: String): List<DigisosSak>

    fun hentDokument(digisosId: String, dokumentlagerId: String, requestedClass: Class<out Any>): Any

}