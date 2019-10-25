package no.nav.sbl.sosialhjelpmodiaapi.mock

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelpmodiaapi.domain.DigisosSak
import no.nav.sbl.sosialhjelpmodiaapi.domain.EttersendtInfoNAV
import no.nav.sbl.sosialhjelpmodiaapi.domain.KommuneInfo
import no.nav.sbl.sosialhjelpmodiaapi.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.*
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.*

@Profile("mock")
@Component
class FiksClientMock : FiksClient {

    private val innsynMap = mutableMapOf<String, DigisosSak>()
    private val dokumentMap = mutableMapOf<String, Any>()

    override fun hentDigisosSak(digisosId: String, sporingsId: String): DigisosSak {
        return innsynMap.getOrElse(digisosId, {
            val default = defaultDigisosSak.copyDigisosSokerWithNewMetadataId(UUID.randomUUID().toString())
            innsynMap[digisosId] = default
            default
        })
    }

    override fun hentDokument(digisosId: String, dokumentlagerId: String, requestedClass: Class<out Any>, sporingsId: String): Any {
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

    override fun hentAlleDigisosSaker(sporingsId: String): List<DigisosSak> {
        return innsynMap.values.toList()
    }

    override fun hentKommuneInfo(kommunenummer: String): KommuneInfo {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun postDigisosSak(digisosSak: DigisosSak) {
        innsynMap[digisosSak.fiksDigisosId] = digisosSak
    }

    fun postDokument(dokumentlagerId: String, jsonDigisosSoker: JsonDigisosSoker) {
        dokumentMap[dokumentlagerId] = jsonDigisosSoker
    }

    fun postDokument(dokumentlagerId: String, jsonVedleggSpesifikasjon: JsonVedleggSpesifikasjon) {
        dokumentMap[dokumentlagerId] = jsonVedleggSpesifikasjon
    }

    fun DigisosSak.copyDigisosSokerWithNewMetadataId(metadata: String): DigisosSak {
        return this.copy(digisosSoker = this.digisosSoker?.copy(metadata = metadata))
    }

    fun DigisosSak.updateEttersendtInfoNAV(ettersendtInfoNAV: EttersendtInfoNAV): DigisosSak {
        return this.copy(ettersendtInfoNAV = ettersendtInfoNAV)
    }
}
