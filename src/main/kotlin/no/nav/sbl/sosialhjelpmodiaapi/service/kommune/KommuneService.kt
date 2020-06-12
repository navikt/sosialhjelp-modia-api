package no.nav.sbl.sosialhjelpmodiaapi.service.kommune

import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.HentKommuneInfo
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksException
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.IKKE_STOTTET_CASE
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.MANGLER_KONFIGURASJON
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_IKKE_MULIG
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SKAL_VISE_FEILSIDE
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SOM_VANLIG
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import org.springframework.stereotype.Component

@Component
class KommuneService(
        private val fiksClient: FiksClient,
        private val hentKommuneInfo: HentKommuneInfo

) {

    fun hentKommuneStatus(fiksDigisosId: String): KommuneStatus {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)

        val kommunenummer: String = digisosSak.kommunenummer
        if (kommunenummer.isEmpty()) {
            log.warn("Forsøkte å hente kommuneStatus, men DigisosSak.kommunenummer er tom")
            throw RuntimeException("KommuneStatus kan ikke hentes uten kommunenummer")
        }

        val kommuneInfo: KommuneInfo
        try {
            kommuneInfo = hentKommuneInfo.hentKommuneInfo(kommunenummer)
        } catch (e: FiksException) {
            return MANGLER_KONFIGURASJON
        }

        return when {
            !kommuneInfo.kanMottaSoknader && !kommuneInfo.kanOppdatereStatus && !kommuneInfo.harMidlertidigDeaktivertMottak && !kommuneInfo.harMidlertidigDeaktivertOppdateringer -> HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
            kommuneInfo.kanMottaSoknader && !kommuneInfo.kanOppdatereStatus && !kommuneInfo.harMidlertidigDeaktivertMottak && !kommuneInfo.harMidlertidigDeaktivertOppdateringer -> SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
            kommuneInfo.kanMottaSoknader && kommuneInfo.kanOppdatereStatus && !kommuneInfo.harMidlertidigDeaktivertMottak && !kommuneInfo.harMidlertidigDeaktivertOppdateringer -> SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
            kommuneInfo.kanMottaSoknader && kommuneInfo.kanOppdatereStatus && kommuneInfo.harMidlertidigDeaktivertMottak && !kommuneInfo.harMidlertidigDeaktivertOppdateringer -> SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SOM_VANLIG
            kommuneInfo.kanMottaSoknader && !kommuneInfo.kanOppdatereStatus && kommuneInfo.harMidlertidigDeaktivertMottak && !kommuneInfo.harMidlertidigDeaktivertOppdateringer -> SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_IKKE_MULIG
            kommuneInfo.kanMottaSoknader && kommuneInfo.kanOppdatereStatus && kommuneInfo.harMidlertidigDeaktivertMottak && kommuneInfo.harMidlertidigDeaktivertOppdateringer -> SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SKAL_VISE_FEILSIDE

            else -> {
                log.warn("Forsøkte å hente kommunestatus, men caset er ikke dekket: $kommuneInfo")
                return IKKE_STOTTET_CASE
            }
        }
    }

    fun hentKommuneInfo(kommunenummer: String): KommuneInfo {
        return hentKommuneInfo.hentKommuneInfo(kommunenummer)
    }

    fun hentAlleKommuneInfo(): List<KommuneInfo> {
        return hentKommuneInfo.hentAlleKommuneInfo()
    }

    companion object {
        private val log by logger()
    }
}

enum class KommuneStatus {
    HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT,
    MANGLER_KONFIGURASJON,
    SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA,
    SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SOM_VANLIG,
    SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_IKKE_MULIG,
    SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SKAL_VISE_FEILSIDE,
    IKKE_STOTTET_CASE
}
