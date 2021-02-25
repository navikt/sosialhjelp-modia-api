package no.nav.sosialhjelp.modia.mock

import no.nav.sosialhjelp.modia.client.norg.NavEnhet
import no.nav.sosialhjelp.modia.client.norg.NorgClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("mock | local")
@Component
class NorgClientMock : NorgClient {

    private val innsynMap = mutableMapOf<String, NavEnhet>()

    override fun hentNavEnhet(enhetsnr: String): NavEnhet {
        return innsynMap.getOrElse(enhetsnr, { defaultNavEnhet(enhetsnr) })
    }

    override fun hentAlleNavEnheter(): List<NavEnhet> {
        return listOf(
                NavEnhet(enhetId = 1, navn = "NAV Longyearbyen", enhetNr = "1001", antallRessurser = 1, status = "AKTIV", aktiveringsdato = "1982-04-21", nedleggelsesdato = "null", sosialeTjenester = sosialetjenesterInfo, type = "LOKAL"),
                NavEnhet(enhetId = 2, navn = "NAV Ny-Ålesund", enhetNr = "1002", antallRessurser = 2, status = "AKTIV", aktiveringsdato = "1982-04-21", nedleggelsesdato = "null", sosialeTjenester = sosialetjenesterInfo, type = "LOKAL"),
                NavEnhet(enhetId = 3, navn = "NAV Spitsbergen", enhetNr = "1003", antallRessurser = 3, status = "AKTIV", aktiveringsdato = "1982-04-21", nedleggelsesdato = "null", sosialeTjenester = sosialetjenesterInfo, type = "LOKAL"),
                NavEnhet(enhetId = 4, navn = "NAV Jan Mayen", enhetNr = "1004", antallRessurser = 4, status = "AKTIV", aktiveringsdato = "1982-04-21", nedleggelsesdato = "null", sosialeTjenester = sosialetjenesterInfo, type = "LOKAL"),
                NavEnhet(enhetId = 5, navn = "NAV Bjørnøya", enhetNr = "1005", antallRessurser = 5, status = "AKTIV", aktiveringsdato = "1982-04-21", nedleggelsesdato = "null", sosialeTjenester = sosialetjenesterInfo, type = "LOKAL"),
                NavEnhet(enhetId = 6, navn = "NAV Dronning Maud land", enhetNr = "1006", antallRessurser = 6, status = "AKTIV", aktiveringsdato = "1982-04-21", nedleggelsesdato = "null", sosialeTjenester = sosialetjenesterInfo, type = "LOKAL"),
                NavEnhet(enhetId = 7, navn = "NAV Bouvetøya", enhetNr = "1007", antallRessurser = 7, status = "AKTIV", aktiveringsdato = "1982-04-21", nedleggelsesdato = "null", sosialeTjenester = sosialetjenesterInfo, type = "Noe annet"),
                NavEnhet(enhetId = 8, navn = "NAV Oslo", enhetNr = "2002", antallRessurser = 8, status = "AKTIV", aktiveringsdato = "1982-04-21", nedleggelsesdato = "null", sosialeTjenester = sosialetjenesterInfo, type = "LOKAL"),
                NavEnhet(enhetId = 9, navn = "NAV Bergen", enhetNr = "3002", antallRessurser = 9, status = "AKTIV", aktiveringsdato = "1982-04-21", nedleggelsesdato = "null", sosialeTjenester = sosialetjenesterInfo, type = "LOKAL"),
                NavEnhet(enhetId = 10, navn = "NAV Trondheim", enhetNr = "4002", antallRessurser = 10, status = "AKTIV", aktiveringsdato = "1982-04-21", nedleggelsesdato = "null", sosialeTjenester = sosialetjenesterInfo, type = "LOKAL"),
                NavEnhet(enhetId = 11, navn = "NAV Stavanger", enhetNr = "5002", antallRessurser = 11, status = "AKTIV", aktiveringsdato = "1982-04-21", nedleggelsesdato = "null", sosialeTjenester = sosialetjenesterInfo, type = "LOKAL"),
                NavEnhet(enhetId = 12, navn = "NAV Tromsø", enhetNr = "6002", antallRessurser = 12, status = "AKTIV", aktiveringsdato = "1982-04-21", nedleggelsesdato = "null", sosialeTjenester = sosialetjenesterInfo, type = "LOKAL"),
        )
    }

    private fun defaultNavEnhet(enhetsnr: String): NavEnhet {
        return NavEnhet(
                enhetId = 100000367,
                navn = "NAV Longyearbyen",
                enhetNr = enhetsnr,
                antallRessurser = 20,
                status = "AKTIV",
                aktiveringsdato = "1982-04-21",
                nedleggelsesdato = "null",
                sosialeTjenester = sosialetjenesterInfo,
                type = "LOKAL"
        )
    }

    private val sosialetjenesterInfo: String = """
        Til saksbehandler:
        Flere vakttelefoner:
        Mottak (nye brukere): 11112222
        Ungdom (18-24år, samt OT-ungdom 16år+): 11112222
        Avklaring (enslige over 20år u/hovedutford. rus/psyk.): 11112222
        Rus/psyk. (m/hovedutford. rus/psyk.): 11112222
        Familie (har barn som bor minst 50% hos bruker): 11112222
        Boligkontor: 11112222
        KVP: 11112222
        Intro. (har stønaden): 11112222
        Sosialfaglige tjenester: Boligkontor (Startlån, bostøtte, kommunal bostøtte, bolig for vanskeligstilte, kommunalt frikort, OT (oppfølgingstenesten), flyktningtjenesten, rus

        Sender post digitalt

        Digital søknad på nav.no/sosialhjelp. Dokumentasjon av vilkår kan ettersendes digitalt. Papir søknadsskjema på kommunens nettside og i V/P - Nye søkere: Ønsker kontakt før innsending av digital søknad - Ønsker kontakt i forkant før søknad om nødhjelp (mat/bolig)

        Saksbehandlingstider: Økonomisk sosialhjelp: 14 dager Startlån: 1mnd.
        Utbetalinger:
        Fast utbetalingsdato: 27-30 i mnd
        Siste tidspunkt for kjøring: 1030
        Utbetaling når utbetaling havner på helg/helligdag: siste virkedag før Utbetalingsmåter for nødhjelp: kronekort/rekvisisjon Kvalifiseringsstønad og introduksjonsstønad: 28 i mnd
    """.trimIndent()
}