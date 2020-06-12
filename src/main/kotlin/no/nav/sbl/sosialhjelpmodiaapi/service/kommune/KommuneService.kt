package no.nav.sbl.sosialhjelpmodiaapi.service.kommune

import kotlinx.coroutines.runBlocking
import no.nav.sbl.sosialhjelpmodiaapi.client.fiks.FiksClient
import no.nav.sbl.sosialhjelpmodiaapi.client.idporten.IdPortenService
import no.nav.sbl.sosialhjelpmodiaapi.common.FiksException
import no.nav.sbl.sosialhjelpmodiaapi.config.ClientProperties
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.IKKE_STOTTET_CASE
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.MANGLER_KONFIGURASJON
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_IKKE_MULIG
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SKAL_VISE_FEILSIDE
import no.nav.sbl.sosialhjelpmodiaapi.service.kommune.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER_INNSYN_SOM_VANLIG
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.kommuneinfo.FiksProperties
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class KommuneService(
        private val fiksClient: FiksClient,
        restTemplate: RestTemplate,
        private val clientProperties: ClientProperties,
        private val idPortenService: IdPortenService

) {

    private val fiksProperties = FiksProperties(
            hentKommuneInfoUrl = clientProperties.fiksDigisosEndpointUrl + PATH_KOMMUNEINFO,
            hentAlleKommuneInfoUrl = clientProperties.fiksDigisosEndpointUrl + PATH_ALLE_KOMMUNEINFO
    )

    private val kommuneInfoClient = KommuneInfoClient(restTemplate, fiksProperties)

    fun hentKommuneStatus(fiksDigisosId: String): KommuneStatus {
        val digisosSak = fiksClient.hentDigisosSak(fiksDigisosId)

        val kommunenummer: String = digisosSak.kommunenummer
        if (kommunenummer.isEmpty()) {
            log.warn("Forsøkte å hente kommuneStatus, men DigisosSak.kommunenummer er tom")
            throw RuntimeException("KommuneStatus kan ikke hentes uten kommunenummer")
        }

        val kommuneInfo: KommuneInfo
        try {
            val headers = IntegrationUtils.fiksHeaders(clientProperties, getToken())
            kommuneInfo = kommuneInfoClient.hentKommuneInfo(kommunenummer, headers)
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
        try {
            val headers = IntegrationUtils.fiksHeaders(clientProperties, getToken())
            return kommuneInfoClient.hentKommuneInfo(kommunenummer, headers)
        } catch (t: Throwable) {
            log.warn("Fiks - hentKommuneInfo feilet - ${t.message}")
            throw FiksException(null, t.message, t)
        }
    }

    fun hentAlleKommuneInfo(): List<KommuneInfo> {
        try {
            val headers = IntegrationUtils.fiksHeaders(clientProperties, getToken())
            return kommuneInfoClient.hentAlleKommuneInfo(headers)
        } catch (t: Throwable) {
            log.warn("Fiks - hentAlleKommuneInfo feilet - ${t.message}", t)
            throw FiksException(null, t.message, t)
        }
    }

    private fun getToken(): String {
        val virksomhetstoken = runBlocking { idPortenService.requestToken() }
        return IntegrationUtils.BEARER + virksomhetstoken.token
    }

    companion object {
        private val log by logger()

        private const val PATH_KOMMUNEINFO = "/digisos/api/v1/nav/kommuner/{kommunenummer}"
        private const val PATH_ALLE_KOMMUNEINFO = "/digisos/api/v1/nav/kommuner"
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
