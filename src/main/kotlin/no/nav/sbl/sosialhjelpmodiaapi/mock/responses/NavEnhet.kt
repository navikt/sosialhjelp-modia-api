package no.nav.sbl.sosialhjelpmodiaapi.mock.responses

import no.nav.sbl.sosialhjelpmodiaapi.domain.NavEnhet

val defaultNAVEnhet = NavEnhet(
        enhetId = 100000367,
        navn = "NAV Åfjord",
        enhetNr = 1630,
        antallRessurser = 20,
        status = "AKTIV",
        aktiveringsdato = "1982-04-21",
        nedleggelsesdato = "null"
)