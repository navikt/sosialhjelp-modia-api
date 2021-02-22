package no.nav.sbl.sosialhjelpmodiaapi.mock

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.defaultDigisosSak
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.digisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.jsonVedleggSpesifikasjonEttersendelse
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.jsonVedleggSpesifikasjonEttersendelse_2
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.jsonVedleggSpesifikasjonSoknad
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.minimalDigisosSoker
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.minimalDigitalsoknad
import no.nav.sbl.sosialhjelpmodiaapi.mock.responses.minimalPapirsoknad
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.EttersendtInfoNAV
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@Profile("mock")
@Component
class FiksClientMock : FiksClient {

    private val innsynMap = initialInnsynMap()
    private val dokumentMap = mutableMapOf<String, Any>()

    fun initialInnsynMap(): MutableMap<String, DigisosSak> {
        val initialInnsynMap = mutableMapOf<String, DigisosSak>()
        initialInnsynMap[defaultDigisosSak.fiksDigisosId] = defaultDigisosSak
        initialInnsynMap[minimalPapirsoknad.fiksDigisosId] = minimalPapirsoknad
        initialInnsynMap[minimalDigitalsoknad.fiksDigisosId] = minimalDigitalsoknad
        return initialInnsynMap
    }

    override fun hentDigisosSak(digisosId: String): DigisosSak {
        return innsynMap.getOrDefault(digisosId, defaultDigisosSak)
    }

    override fun hentDokument(fnr: String, digisosId: String, dokumentlagerId: String, requestedClass: Class<out Any>): Any {
        return when (requestedClass) {
            JsonDigisosSoker::class.java -> hentDigisosSoker(dokumentlagerId)
            JsonVedleggSpesifikasjon::class.java -> hentVedleggSpesifikasjon(dokumentlagerId)
            else -> requestedClass.getDeclaredConstructor(requestedClass).newInstance()
        }
    }

    fun hentDigisosSoker(dokumentlagerId: String): Any {
        return when (dokumentlagerId) {
            "mock-digisossoker" -> dokumentMap.getOrDefault(dokumentlagerId, digisosSoker)
            "mock-digisossoker-minimal" -> dokumentMap.getOrDefault(dokumentlagerId, minimalDigisosSoker)
            else -> dokumentMap.getOrDefault(dokumentlagerId, digisosSoker)
        }
    }

    fun hentVedleggSpesifikasjon(dokumentlagerId: String): Any {
        return when (dokumentlagerId) {
            "mock-soknad-vedlegg-metadata" -> dokumentMap.getOrDefault(dokumentlagerId, jsonVedleggSpesifikasjonSoknad)
            "mock-ettersendelse-vedlegg-metadata" -> dokumentMap.getOrDefault(dokumentlagerId, jsonVedleggSpesifikasjonEttersendelse)
            else -> dokumentMap.getOrDefault(dokumentlagerId, jsonVedleggSpesifikasjonEttersendelse_2)
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
