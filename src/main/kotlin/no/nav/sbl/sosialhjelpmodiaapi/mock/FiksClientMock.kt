package no.nav.sbl.sosialhjelpmodiaapi.mock

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.defaultDigisosSak
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.defaultJsonSoknad
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.digisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.jsonVedleggSpesifikasjonEttersendelse
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.jsonVedleggSpesifikasjonEttersendelse_2
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.jsonVedleggSpesifikasjonSoknad
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.EttersendtInfoNAV
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
