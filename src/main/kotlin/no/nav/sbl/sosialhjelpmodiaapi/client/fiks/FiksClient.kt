package no.nav.sbl.sosialhjelpmodiaapi.client.fiks

import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.KommuneInfo

interface FiksClient {

    fun hentDigisosSak(digisosId: String): DigisosSak

    fun hentAlleDigisosSaker(fnr: String): List<DigisosSak>

    fun hentKommuneInfo(kommunenummer: String): KommuneInfo

    fun hentDokument(digisosId: String, dokumentlagerId: String, requestedClass: Class<out Any>): Any

    fun hentKommuneInfoForAlle(): List<KommuneInfo>
}