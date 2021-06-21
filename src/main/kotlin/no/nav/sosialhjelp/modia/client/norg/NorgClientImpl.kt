package no.nav.sosialhjelp.modia.client.norg

import no.nav.sosialhjelp.modia.common.NorgException
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.redis.ALLE_NAVENHETER_CACHE_KEY
import no.nav.sosialhjelp.modia.redis.ALLE_NAVENHETER_CACHE_TIME_TO_LIVE_SECONDS
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.typeRef
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_CALL_ID
import no.nav.sosialhjelp.modia.utils.mdc.MDCUtils.getCallId
import no.nav.sosialhjelp.modia.utils.objectMapper
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Profile("!(mock | local)")
@Component
class NorgClientImpl(
    private val norgWebClient: WebClient,
    private val redisService: RedisService
) : NorgClient {

    override fun hentNavEnhet(enhetsnr: String): NavEnhet? {
        if (enhetsnr == "") return null

        return norgWebClient.get()
            .uri {
                it
                    .path("/enhet/{enhetsnr}")
                    .build("enhetsnr" to enhetsnr)
            }
            .header(HEADER_CALL_ID, getCallId())
            .retrieve()
            .bodyToMono<NavEnhet>()
            .onErrorMap { e ->
                when (e) {
                    is WebClientResponseException -> log.warn("Norg2 - Noe feilet - ${e.statusCode} ${e.statusText}", e)
                    else -> log.warn("Norg2 - Noe feilet", e)
                }
                NorgException(e.message, e)
            }
            .block()!!
            .also { log.debug("Norg2 - GET enhet $enhetsnr OK") }
    }

    override fun hentAlleNavEnheter(): List<NavEnhet> {
        return norgWebClient.get()
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
            }
            .block()!!
            .also { lagreTilCache(it) }
    }

    private fun lagreTilCache(list: List<NavEnhet>) {
        redisService.set(ALLE_NAVENHETER_CACHE_KEY, objectMapper.writeValueAsBytes(list), ALLE_NAVENHETER_CACHE_TIME_TO_LIVE_SECONDS)
    }

    companion object {
        private val log by logger()
    }
}
