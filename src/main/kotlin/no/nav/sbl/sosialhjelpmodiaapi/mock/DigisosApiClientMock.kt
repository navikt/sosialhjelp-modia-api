package no.nav.sbl.sosialhjelpmodiaapi.mock

import no.nav.sbl.sosialhjelpmodiaapi.client.digisosapi.DigisosApiClient
import no.nav.sbl.sosialhjelpmodiaapi.toLocalDateTime
import no.nav.sbl.sosialhjelpmodiaapi.utils.DigisosApiWrapper
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.DigisosSoker
import no.nav.sosialhjelp.api.fiks.DokumentInfo
import no.nav.sosialhjelp.api.fiks.EttersendtInfoNAV
import no.nav.sosialhjelp.api.fiks.OriginalSoknadNAV
import no.nav.sosialhjelp.api.fiks.Tilleggsinformasjon
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.util.*

@Profile("mock")
@Component
class DigisosApiClientMock(
        private val fiksClientMock: FiksClientMock
) : DigisosApiClient {

    override fun oppdaterDigisosSak(fiksDigisosId: String?, digisosApiWrapper: DigisosApiWrapper): String? {
        val dokumentlagerId = UUID.randomUUID().toString()
        fiksClientMock.postDokument(dokumentlagerId, digisosApiWrapper.sak.soker)
        var id = fiksDigisosId
        if (id == null) {
            id = UUID.randomUUID().toString()
        }

        fiksClientMock.postDigisosSak(DigisosSak(
                fiksDigisosId = id,
                sokerFnr = "01234567890",
                fiksOrgId = "11415cd1-e26d-499a-8421-751457dfcbd5",
                kommunenummer = "1",
                sistEndret = System.currentTimeMillis(),
                originalSoknadNAV = OriginalSoknadNAV("110000000", "", "mock-soknad-vedlegg-metadata", DokumentInfo("", "", 0L), Collections.emptyList(),
                        femMinutterForMottattSoknad(digisosApiWrapper)),
                ettersendtInfoNAV = EttersendtInfoNAV(Collections.emptyList()),
                digisosSoker = DigisosSoker(dokumentlagerId, Collections.emptyList(), System.currentTimeMillis()),
                tilleggsinformasjon = Tilleggsinformasjon("1234")
        ))
        return id
    }

    private fun femMinutterForMottattSoknad(digisosApiWrapper: DigisosApiWrapper): Long {
        val mottattTidspunkt = digisosApiWrapper.sak.soker.hendelser.minBy { it.hendelsestidspunkt }!!.hendelsestidspunkt
        return try {
            mottattTidspunkt.toLocalDateTime().minusMinutes(5).atZone(ZoneId.of("Europe/Oslo")).toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) {
            LocalDateTime.now().minusMinutes(5).atZone(ZoneId.of("Europe/Oslo")).toInstant().toEpochMilli()
        }
    }
}
