package no.nav.sbl.sosialhjelpmodiaapi.mock

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.kommuneinfo.FiksProperties
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Profile("mock")
@Component
class KommuneInfoClientMock : KommuneInfoClient {

    override val fiksProperties: FiksProperties
        get() = TODO("Not yet implemented")
    override val restTemplate: RestTemplate
        get() = TODO("Not yet implemented")

    override fun get(kommunenummer: String): KommuneInfo {
        return KommuneInfo(kommunenummer, true, true, false, false, null, true, null)
    }

    override fun getAll(): List<KommuneInfo> {
        val returnValue = ArrayList<KommuneInfo>()
        returnValue.add(KommuneInfo("0001", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("1123", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("0002", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("9863", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("9999", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("2352", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("0000", true, false, false, false, null, true, null))
        returnValue.add(KommuneInfo("8734", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("0909", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("0301", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("1222", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("9002", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("6663", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("1201", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("4455", true, true, false, true, null, true, null))
        returnValue.add(KommuneInfo("1833", false, false, false, false, null, true, null))
        returnValue.add(KommuneInfo("1430", true, true, true, true, null, true, null))
        return returnValue
    }
}