package no.nav.sbl.sosialhjelpmodiaapi.mock

import no.nav.sbl.sosialhjelpmodiaapi.domain.NavEnhet
import no.nav.sbl.sosialhjelpmodiaapi.norg.NorgClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("mock | local")
@Component
class NorgClientMock : NorgClient {

    private val innsynMap = mutableMapOf<String, NavEnhet>()

    override fun hentNavEnhet(enhetsnr: String): NavEnhet {
        return innsynMap.getOrElse(enhetsnr, { defaultNavEnhet(enhetsnr) })
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
                sosialeTjenester = sosialetjenesterInfo
        )
    }

    private val sosialetjenesterInfo: String = """
        Til saksbehandler:
        Flere vakttelefoner:ø
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