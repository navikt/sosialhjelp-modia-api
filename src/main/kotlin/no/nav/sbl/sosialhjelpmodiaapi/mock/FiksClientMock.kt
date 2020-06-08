package no.nav.sbl.sosialhjelpmodiaapi.mock

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelpmodiaapi.domain.DigisosSak
import no.nav.sbl.sosialhjelpmodiaapi.domain.EttersendtInfoNAV
import no.nav.sbl.sosialhjelpmodiaapi.domain.KommuneInfo
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.defaultDigisosSak
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.defaultJsonSoknad
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.digisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.jsonVedleggSpesifikasjonEttersendelse
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.jsonVedleggSpesifikasjonEttersendelse_2
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.jsonVedleggSpesifikasjonSoknad
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Profile("mock")
@Component
class FiksClientMock : FiksClient {

    private val innsynMap = mutableMapOf<String, DigisosSak>()
    private val dokumentMap = mutableMapOf<String, Any>()

    override fun hentDigisosSak(digisosId: String): DigisosSak {
        return innsynMap.getOrElse(digisosId, {
            val default = defaultDigisosSak.copyDigisosSokerWithNewMetadataId(digisosId, innsynMap.size.toLong())
            innsynMap[digisosId] = default
            default
        })
    }

    override fun hentDokument(digisosId: String, dokumentlagerId: String, requestedClass: Class<out Any>): Any {
        return when (requestedClass) {
            JsonDigisosSoker::class.java -> dokumentMap.getOrElse(dokumentlagerId, {
                val default = digisosSoker
                dokumentMap[dokumentlagerId] = default
                default
            })
            JsonSoknad::class.java -> dokumentMap.getOrElse(dokumentlagerId, {
                val default = defaultJsonSoknad
                dokumentMap[dokumentlagerId] = default
                default
            })
            JsonVedleggSpesifikasjon::class.java ->
                when (dokumentlagerId) {
                    "mock-soknad-vedlegg-metadata" -> dokumentMap.getOrElse(dokumentlagerId, {
                        val default = jsonVedleggSpesifikasjonSoknad
                        dokumentMap[dokumentlagerId] = default
                        default
                    })
                    "mock-ettersendelse-vedlegg-metadata" -> dokumentMap.getOrElse(dokumentlagerId, {
                        val default = jsonVedleggSpesifikasjonEttersendelse
                        dokumentMap[dokumentlagerId] = default
                        default
                    })
                    else -> dokumentMap.getOrElse(dokumentlagerId, {
                        val default = jsonVedleggSpesifikasjonEttersendelse_2
                        dokumentMap[dokumentlagerId] = default
                        default
                    })
                }
            else -> requestedClass.getDeclaredConstructor(requestedClass).newInstance()
        }
    }

    override fun hentAlleDigisosSaker(fnr: String): List<DigisosSak> {
        return when {
            innsynMap.values.isEmpty() -> listOf(defaultDigisosSak.copyDigisosSokerWithNewMetadataId(UUID.randomUUID().toString(), 1))
            else -> innsynMap.values.toList()
        }
    }

    override fun hentKommuneInfo(kommunenummer: String): KommuneInfo {
        return KommuneInfo(kommunenummer, true, true, false, false, null)
    }

    override fun hentKommuneInfoForAlle(): List<KommuneInfo> {
        val returnValue = ArrayList<KommuneInfo>()
        returnValue.add(KommuneInfo("0001", true, true, false, false, null))
        returnValue.add(KommuneInfo("1123", true, true, false, false, null))
        returnValue.add(KommuneInfo("0002", true, true, false, false, null))
        returnValue.add(KommuneInfo("9863", true, true, false, false, null))
        returnValue.add(KommuneInfo("9999", true, true, false, false, null))
        returnValue.add(KommuneInfo("2352", true, true, false, false, null))
        returnValue.add(KommuneInfo("0000", true, false, false, false, null))
        returnValue.add(KommuneInfo("8734", true, true, false, false, null))
        returnValue.add(KommuneInfo("0909", true, true, false, false, null))
        returnValue.add(KommuneInfo("0301", true, true, false, false, null))
        returnValue.add(KommuneInfo("1222", true, true, false, false, null))
        returnValue.add(KommuneInfo("9002", true, true, false, false, null))
        returnValue.add(KommuneInfo("6663", true, true, false, false, null))
        returnValue.add(KommuneInfo("1201", true, true, false, false, null))
        returnValue.add(KommuneInfo("4455", true, true, false, true, null))
        returnValue.add(KommuneInfo("1833", false, false, false, false, null))
        returnValue.add(KommuneInfo("1430", true, true, true, true, null))
        return returnValue
    }

    fun postDigisosSak(digisosSak: DigisosSak) {
        innsynMap[digisosSak.fiksDigisosId] = digisosSak
    }

    fun digisosSakFinnes(fiksDigisosId: String): Boolean {
        return innsynMap.containsKey(fiksDigisosId)
    }

    fun postDokument(dokumentlagerId: String, jsonDigisosSoker: JsonDigisosSoker) {
        dokumentMap[dokumentlagerId] = jsonDigisosSoker
    }

    fun postDokument(dokumentlagerId: String, jsonVedleggSpesifikasjon: JsonVedleggSpesifikasjon) {
        dokumentMap[dokumentlagerId] = jsonVedleggSpesifikasjon
    }

    fun DigisosSak.copyDigisosSokerWithNewMetadataId(metadata: String, ukerTilbake: Long): DigisosSak {
        val sistEndret = LocalDateTime.now().minusWeeks(ukerTilbake).toEpochSecond(ZoneOffset.UTC) * 1000
        return this.copy(fiksDigisosId = metadata, sistEndret = sistEndret, digisosSoker = this.digisosSoker?.copy(metadata = metadata))
    }

    fun DigisosSak.updateEttersendtInfoNAV(ettersendtInfoNAV: EttersendtInfoNAV): DigisosSak {
        return this.copy(ettersendtInfoNAV = ettersendtInfoNAV)
    }
}
