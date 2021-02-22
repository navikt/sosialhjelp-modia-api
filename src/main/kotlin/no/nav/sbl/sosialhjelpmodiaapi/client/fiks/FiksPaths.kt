package no.nav.sbl.sosialhjelpmodiaapi.client.fiks

object FiksPaths {

    const val PATH_DIGISOSSAK = "/digisos/api/v1/nav/soknader/{digisosId}"
    const val PATH_ALLE_DIGISOSSAKER = "/digisos/api/v1/nav/soknader/soknader"
    const val PATH_DOKUMENT = "/digisos/api/v1/nav/soknader/{digisosId}/dokumenter/{dokumentlagerId}"
    const val PATH_KOMMUNEINFO = "/digisos/api/v1/nav/kommuner/{kommunenummer}"
    const val PATH_ALLE_KOMMUNEINFO = "/digisos/api/v1/nav/kommuner"
}
