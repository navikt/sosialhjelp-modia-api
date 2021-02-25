package no.nav.sosialhjelp.modia.client.norg

import no.nav.sosialhjelp.modia.common.NorgException
import no.nav.sosialhjelp.modia.config.ClientProperties
import no.nav.sosialhjelp.modia.domain.NavEnhet
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.redis.ALLE_NAVENHETER_CACHE_KEY
import no.nav.sosialhjelp.modia.redis.ALLE_NAVENHETER_CACHE_TIME_TO_LIVE_SECONDS
import no.nav.sosialhjelp.modia.redis.RedisService
import no.nav.sosialhjelp.modia.typeRef
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.HEADER_CALL_ID
import no.nav.sosialhjelp.modia.utils.IntegrationUtils.forwardHeaders
import no.nav.sosialhjelp.modia.utils.mdc.MDCUtils.getCallId
import no.nav.sosialhjelp.modia.utils.objectMapper
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate


@Profile("!mock & !local")
@Component
class NorgClientImpl(
        clientProperties: ClientProperties,
        private val restTemplate: RestTemplate,
        private val redisService: RedisService
) : NorgClient {

    private val baseUrl = clientProperties.norgEndpointUrl

    override fun hentNavEnhet(enhetsnr: String): NavEnhet? {
        if (enhetsnr == "") return null

        try {
            val urlTemplate = "$baseUrl/enhet/{enhetsnr}"
            val vars = mapOf("enhetsnr" to enhetsnr)
            val requestEntity = createRequestEntity()
            val response = restTemplate.exchange(urlTemplate, HttpMethod.GET, requestEntity, NavEnhet::class.java, vars)

            log.debug("Norg2 - GET enhet $enhetsnr OK")
            return response.body!!

        } catch (e: HttpStatusCodeException) {
            log.warn("Norg2 - Noe feilet - ${e.statusCode} ${e.statusText}", e)
            throw NorgException(e.message, e)
        } catch (e: Exception) {
            log.warn("Norg2 - Noe feilet", e)
            throw NorgException(e.message, e)
        }
    }

    override fun hentAlleNavEnheter(): List<NavEnhet> {
        try {
            val urlTemplate = "$baseUrl/enhet?enhetStatusListe=AKTIV"
            val requestEntity = createRequestEntity()
            val response = restTemplate.exchange(urlTemplate, HttpMethod.GET, requestEntity, typeRef<List<NavEnhet>>())

            return response.body!!
                    .also { lagreTilCache(it) }
        } catch (e: HttpStatusCodeException) {
            log.warn("Norg2 - Noe feilet - ${e.statusCode} ${e.statusText}", e)
            throw NorgException(e.message, e)
        } catch (e: Exception) {
            log.warn("Norg2 - Noe feilet", e)
            throw NorgException(e.message, e)
        }
    }

    private fun createRequestEntity(): HttpEntity<Nothing> {
        val headers = forwardHeaders()
        headers.set(HEADER_CALL_ID, getCallId())
        return HttpEntity(headers)
    }

    private fun lagreTilCache(list: List<NavEnhet>) {
        redisService.set(ALLE_NAVENHETER_CACHE_KEY, objectMapper.writeValueAsBytes(list), ALLE_NAVENHETER_CACHE_TIME_TO_LIVE_SECONDS)
    }

    companion object {
        private val log by logger()
    }
}
