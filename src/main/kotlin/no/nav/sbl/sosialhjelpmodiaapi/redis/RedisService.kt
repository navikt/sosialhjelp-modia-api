package no.nav.sbl.sosialhjelpmodiaapi.redis

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sbl.sosialhjelpmodiaapi.common.DigisosSakTilhorerAnnenBrukerException
import no.nav.sbl.sosialhjelpmodiaapi.domain.NavEnhet
import no.nav.sbl.sosialhjelpmodiaapi.logger
import no.nav.sbl.sosialhjelpmodiaapi.utils.TokenUtils
import no.nav.sbl.sosialhjelpmodiaapi.utils.objectMapper
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.IOException

interface RedisService {
    val defaultTimeToLiveSeconds: Long
    fun get(key: String, requestedClass: Class<out Any>): Any?
    fun set(key: String, value: ByteArray, timeToLive: Long = defaultTimeToLiveSeconds)
    fun getAlleNavEnheter(): List<NavEnhet>?
}

@Profile("!no-redis")
@Component
class RedisServiceImpl(
        private val redisStore: RedisStore,
        cacheProperties: CacheProperties,
        private val tokenUtils: TokenUtils
) : RedisService {

    override val defaultTimeToLiveSeconds = cacheProperties.timeToLiveSeconds

    override fun get(key: String, requestedClass: Class<out Any>): Any? {
        val bytes: ByteArray? = redisStore.get(key) // Redis har konfigurert timout for disse.
        return if (bytes != null) {
            try {
                val obj = objectMapper.readValue(bytes, requestedClass)
                valider(obj)
                log.debug("Hentet ${requestedClass.simpleName} fra cache, key=$key")
                obj
            } catch (e: IOException) {
                log.warn("Fant key=$key i cache, men value var ikke ${requestedClass.simpleName}")
                null
            } catch (e: DigisosSakTilhorerAnnenBrukerException) {
                log.warn("DigisosSak i cache tilhører en annen bruker enn brukeren fra token.")
                return null
            }
        } else {
            null
        }
    }

    override fun set(key: String, value: ByteArray, timeToLive: Long) {
        val result = redisStore.set(key, value, timeToLive)
        if (result == null) {
            log.warn("Cache put feilet eller fikk timeout")
        } else if (result == "OK") {
            log.debug("Cache put OK $key")
        }
    }

    override fun getAlleNavEnheter(): List<NavEnhet>? {
        val bytes: ByteArray? = redisStore.get(ALLE_NAVENHETER_CACHE_KEY)
        return if (bytes != null) {
            try {
                objectMapper.readValue(bytes, jacksonTypeRef<List<NavEnhet>>())
            } catch (e: IOException) {
                log.warn("Fant key=$ALLE_NAVENHETER_CACHE_KEY, men feil oppstod ved deserialisering til List<NavEnhet>")
                null
            }
        } else null
    }

    /**
     * Kaster feil hvis det finnes additionalProperties på mappet objekt.
     * Tyder på at noe feil har skjedd ved mapping.
     */
    private fun valider(obj: Any?) {
        when {
            obj is DigisosSak && obj.sokerFnr != tokenUtils.hentNavIdentForInnloggetBruker() -> throw DigisosSakTilhorerAnnenBrukerException("DigisosSak tilhører annen bruker")
            obj is JsonDigisosSoker && obj.additionalProperties.isNotEmpty() -> throw IOException("JsonDigisosSoker har ukjente properties - må tilhøre ett annet objekt. Cache-value tas ikke i bruk")
            obj is JsonVedleggSpesifikasjon && obj.additionalProperties.isNotEmpty() -> throw IOException("JsonVedleggSpesifikasjon har ukjente properties - må tilhøre ett annet objekt. Cache-value tas ikke i bruk")
        }
    }

    companion object {
        private val log by logger()
    }
}

@Profile("no-redis")
@Component
class RedisServiceMock : RedisService {

    override val defaultTimeToLiveSeconds: Long = 1L

    override fun get(key: String, requestedClass: Class<out Any>): Any? {
        return null
    }

    override fun set(key: String, value: ByteArray, timeToLive: Long) {

    }

    override fun getAlleNavEnheter(): List<NavEnhet>? {
        return null
    }
}