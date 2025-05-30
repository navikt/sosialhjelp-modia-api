package no.nav.sosialhjelp.modia.navkontor.norg

import no.nav.sosialhjelp.modia.app.client.ClientProperties
import no.nav.sosialhjelp.modia.app.client.unproxiedHttpClient
import no.nav.sosialhjelp.modia.app.exceptions.NorgException
import no.nav.sosialhjelp.modia.app.mdc.MDCUtils.getCallId
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.redis.ALLE_NAVENHETER_CACHE_KEY
import no.nav.sosialhjelp.modia.redis.NAVENHET_CACHE_KEY_PREFIX
import no.nav.sosialhjelp.modia.redis.NAVENHET_CACHE_TIME_TO_LIVE_SECONDS
import no.nav.sosialhjelp.modia.redis.RedisKeyType
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.typeRef
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_CALL_ID
import no.nav.sosialhjelp.modia.utils.objectMapper
import org.springframework.context.annotation.Profile
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

interface NorgClient {
    fun hentNavEnhet(enhetsnr: String): NavEnhet?

    fun hentAlleNavEnheter(): List<NavEnhet>

    fun ping()
}

@Profile("!local")
@Component
class NorgClientImpl(
    webClientBuilder: WebClient.Builder,
    clientProperties: ClientProperties,
    private val redisService: RedisService,
) : NorgClient {
    private val norgWebClient =
        webClientBuilder
            .clientConnector(ReactorClientHttpConnector(unproxiedHttpClient()))
            .codecs {
                it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            }.baseUrl(clientProperties.norgEndpointUrl)
            .build()

    override fun hentNavEnhet(enhetsnr: String): NavEnhet? {
        if (enhetsnr.isEmpty()) return null

        return hentNavEnhetFraCache(enhetsnr) ?: hentNavEnhetFraServer(enhetsnr)
    }

    private fun hentNavEnhetFraCache(enhetsnr: String): NavEnhet? =
        redisService.get(RedisKeyType.NORG_CLIENT, "$NAVENHET_CACHE_KEY_PREFIX$enhetsnr", NavEnhet::class.java)

    private fun hentNavEnhetFraServer(enhetsnr: String): NavEnhet =
        norgWebClient
            .get()
            .uri("/enhet/{enhetsnr}", enhetsnr)
            .header(HEADER_CALL_ID, getCallId())
            .retrieve()
            .bodyToMono<NavEnhet>()
            .onErrorMap { e ->
                when (e) {
                    is WebClientResponseException -> log.warn("Norg2 - Noe feilet - ${e.statusCode} ${e.statusText}", e)
                    else -> log.warn("Norg2 - Noe feilet", e)
                }
                NorgException(e.message, e)
            }.block()!!
            .also {
                log.debug("Norg2 - GET enhet $enhetsnr OK")
                lagreNavEnhetTilCache(enhetsnr, it)
            }

    override fun hentAlleNavEnheter(): List<NavEnhet> =
        norgWebClient
            .get()
            .uri("/enhet?enhetStatusListe=AKTIV")
            .header(HEADER_CALL_ID, getCallId())
            .retrieve()
            .bodyToMono(typeRef<List<NavEnhet>>())
            .onErrorMap { e ->
                when (e) {
                    is WebClientResponseException -> log.warn("Norg2 - Noe feilet - ${e.statusCode} ${e.statusText}", e)
                    else -> log.warn("Norg2 - Noe feilet", e)
                }
                NorgException(e.message, e)
            }.block()!!
            .also { lagreNavEnhetListeTilCache(it) }

    override fun ping() {
        norgWebClient
            .get()
            .uri("/kodeverk/EnhetstyperNorg")
            .header(HEADER_CALL_ID, getCallId())
            .retrieve()
            .bodyToMono<String>()
            .block()
    }

    private fun lagreNavEnhetListeTilCache(list: List<NavEnhet>) {
        redisService.set(
            type = RedisKeyType.NORG_CLIENT,
            key = ALLE_NAVENHETER_CACHE_KEY,
            value = objectMapper.writeValueAsBytes(list),
            timeToLive = NAVENHET_CACHE_TIME_TO_LIVE_SECONDS,
        )
    }

    private fun lagreNavEnhetTilCache(
        enhetsnr: String,
        navEnhet: NavEnhet,
    ) {
        log.info("Lagrer NavEnhet=$enhetsnr til cache")
        redisService.set(
            type = RedisKeyType.NORG_CLIENT,
            key = "$NAVENHET_CACHE_KEY_PREFIX$enhetsnr",
            value = objectMapper.writeValueAsBytes(navEnhet),
            timeToLive = NAVENHET_CACHE_TIME_TO_LIVE_SECONDS,
        )
    }

    companion object {
        private val log by logger()
    }
}

@Profile("local")
@Component
class NorgClientMock : NorgClient {
    private val innsynMap = mutableMapOf<String, NavEnhet>()

    override fun hentNavEnhet(enhetsnr: String): NavEnhet = innsynMap.getOrElse(enhetsnr, { defaultNavEnhet(enhetsnr) })

    override fun hentAlleNavEnheter(): List<NavEnhet> =
        listOf(
            NavEnhet(
                enhetId = 1,
                navn = "Nav Longyearbyen",
                enhetNr = "1001",
                antallRessurser = 1,
                status = "AKTIV",
                aktiveringsdato = "1982-04-21",
                nedleggelsesdato = "null",
                sosialeTjenester = sosialetjenesterInfo,
                type = "LOKAL",
            ),
            NavEnhet(
                enhetId = 2,
                navn = "Nav Ny-Ålesund",
                enhetNr = "1002",
                antallRessurser = 2,
                status = "AKTIV",
                aktiveringsdato = "1982-04-21",
                nedleggelsesdato = "null",
                sosialeTjenester = sosialetjenesterInfo,
                type = "LOKAL",
            ),
            NavEnhet(
                enhetId = 3,
                navn = "Nav Spitsbergen",
                enhetNr = "1003",
                antallRessurser = 3,
                status = "AKTIV",
                aktiveringsdato = "1982-04-21",
                nedleggelsesdato = "null",
                sosialeTjenester = sosialetjenesterInfo,
                type = "LOKAL",
            ),
            NavEnhet(
                enhetId = 4,
                navn = "Nav Jan Mayen",
                enhetNr = "1004",
                antallRessurser = 4,
                status = "AKTIV",
                aktiveringsdato = "1982-04-21",
                nedleggelsesdato = "null",
                sosialeTjenester = sosialetjenesterInfo,
                type = "LOKAL",
            ),
            NavEnhet(
                enhetId = 5,
                navn = "Nav Bjørnøya",
                enhetNr = "1005",
                antallRessurser = 5,
                status = "AKTIV",
                aktiveringsdato = "1982-04-21",
                nedleggelsesdato = "null",
                sosialeTjenester = sosialetjenesterInfo,
                type = "LOKAL",
            ),
            NavEnhet(
                enhetId = 6,
                navn = "Nav Dronning Maud land",
                enhetNr = "1006",
                antallRessurser = 6,
                status = "AKTIV",
                aktiveringsdato = "1982-04-21",
                nedleggelsesdato = "null",
                sosialeTjenester = sosialetjenesterInfo,
                type = "LOKAL",
            ),
            NavEnhet(
                enhetId = 7,
                navn = "Nav Bouvetøya",
                enhetNr = "1007",
                antallRessurser = 7,
                status = "AKTIV",
                aktiveringsdato = "1982-04-21",
                nedleggelsesdato = "null",
                sosialeTjenester = sosialetjenesterInfo,
                type = "Noe annet",
            ),
            NavEnhet(
                enhetId = 8,
                navn = "Nav Oslo",
                enhetNr = "2002",
                antallRessurser = 8,
                status = "AKTIV",
                aktiveringsdato = "1982-04-21",
                nedleggelsesdato = "null",
                sosialeTjenester = sosialetjenesterInfo,
                type = "LOKAL",
            ),
            NavEnhet(
                enhetId = 9,
                navn = "Nav Bergen",
                enhetNr = "3002",
                antallRessurser = 9,
                status = "AKTIV",
                aktiveringsdato = "1982-04-21",
                nedleggelsesdato = "null",
                sosialeTjenester = sosialetjenesterInfo,
                type = "LOKAL",
            ),
            NavEnhet(
                enhetId = 10,
                navn = "Nav Trondheim",
                enhetNr = "4002",
                antallRessurser = 10,
                status = "AKTIV",
                aktiveringsdato = "1982-04-21",
                nedleggelsesdato = "null",
                sosialeTjenester = sosialetjenesterInfo,
                type = "LOKAL",
            ),
            NavEnhet(
                enhetId = 11,
                navn = "Nav Stavanger",
                enhetNr = "5002",
                antallRessurser = 11,
                status = "AKTIV",
                aktiveringsdato = "1982-04-21",
                nedleggelsesdato = "null",
                sosialeTjenester = sosialetjenesterInfo,
                type = "LOKAL",
            ),
            NavEnhet(
                enhetId = 12,
                navn = "Nav Tromsø",
                enhetNr = "6002",
                antallRessurser = 12,
                status = "AKTIV",
                aktiveringsdato = "1982-04-21",
                nedleggelsesdato = "null",
                sosialeTjenester = sosialetjenesterInfo,
                type = "LOKAL",
            ),
        )

    override fun ping() {
        // no-op
    }

    private fun defaultNavEnhet(enhetsnr: String): NavEnhet =
        NavEnhet(
            enhetId = 100000367,
            navn = "Nav Longyearbyen",
            enhetNr = enhetsnr,
            antallRessurser = 20,
            status = "AKTIV",
            aktiveringsdato = "1982-04-21",
            nedleggelsesdato = "null",
            sosialeTjenester = sosialetjenesterInfo,
            type = "LOKAL",
        )

    private val sosialetjenesterInfo: String =
        """
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
