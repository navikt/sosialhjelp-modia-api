package no.nav.sosialhjelp.modia.mock

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("mock")
@Component
class KommuneInfoClientMock : KommuneInfoClient {

    override fun get(kommunenummer: String, token: String): KommuneInfo {
        if (kommunenummer == "0301") {
            return KommuneInfo(kommunenummer, true, true, false, false, null, true, "Nabo")
        }
        return KommuneInfo(kommunenummer, true, true, false, false, null, true, null)
    }

    override fun getAll(token: String): List<KommuneInfo> {
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
        returnValue.add(KommuneInfo("0301", true, true, false, false, null, true, "En annen kommune"))
        returnValue.add(KommuneInfo("1222", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("9002", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("6663", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("1201", true, true, false, false, null, true, null))
        returnValue.add(KommuneInfo("4455", true, true, false, true, null, true, null))
        returnValue.add(KommuneInfo("1833", false, false, false, false, null, true, null))
        returnValue.add(KommuneInfo("1430", true, true, true, true, null, true, null))
        returnValue.add(KommuneInfo("0003", true, true, false, false, null, true, "Nabo"))
        return returnValue
    }
}