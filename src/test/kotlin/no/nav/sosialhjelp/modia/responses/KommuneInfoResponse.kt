package no.nav.sosialhjelp.modia.responses

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sosialhjelp.api.fiks.KommuneInfo

val kommuneInfoResponseString =
    KommuneInfo(
        kommunenummer = "1234",
        kanMottaSoknader = true,
        kanOppdatereStatus = true,
        harMidlertidigDeaktivertMottak = false,
        harMidlertidigDeaktivertOppdateringer = false,
        kontaktpersoner = null,
        harNksTilgang = true,
        behandlingsansvarlig = null,
    ).let { JsonSosialhjelpObjectMapper.createObjectMapper().writeValueAsString(it) }
