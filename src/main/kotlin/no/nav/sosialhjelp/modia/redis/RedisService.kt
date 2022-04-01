package no.nav.sosialhjelp.modia.redis

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.modia.client.norg.NavEnhet
import no.nav.sosialhjelp.modia.common.DigisosSakTilhorerAnnenBrukerException
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.utils.objectMapper
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.IOException

enum class RedisKeyType {
    AZUREDINGS,
    SKJERMEDE_PERSONER,
    FIKS_CLIENT,
    NORG_CLIENT,
    KOMMUNE_SERVICE
}

interface RedisService {
    val defaultTimeToLiveSeconds: Long
    fun get(type: RedisKeyType, key: String, requestedClass: Class<out Any>): Any?
    fun set(type: RedisKeyType, key: String, value: ByteArray, timeToLive: Long = defaultTimeToLiveSeconds)
    fun getAlleNavEnheter(): List<NavEnhet>?
}

@Profile("!no-redis")
@Component
class RedisServiceImpl(
    private val redisStore: RedisStore,
    cacheProperties: CacheProperties,
) : RedisService {

    override val defaultTimeToLiveSeconds = cacheProperties.timeToLiveSeconds

    override fun get(type: RedisKeyType, key: String, requestedClass: Class<out Any>): Any? {
        val bytes: ByteArray? = redisStore.get("${type.name}_$key") // Redis har konfigurert timout for disse.
        return if (bytes != null) {
            try {
                val obj = objectMapper.readValue(bytes, requestedClass)
                valider(obj)
                log.debug("Hentet ${requestedClass.simpleName} fra cache, type=${type.name}")
                obj
            } catch (e: IOException) {
                log.warn("Fant type=${type.name} i cache, men value var ikke ${requestedClass.simpleName}")
                null
            } catch (e: DigisosSakTilhorerAnnenBrukerException) {
                log.warn("DigisosSak i cache tilhører en annen bruker enn brukeren fra token.")
                return null
            }
        } else {
            null
        }
    }

    override fun set(type: RedisKeyType, key: String, value: ByteArray, timeToLive: Long) {
        val result = redisStore.set("${type.name}_$key", value, timeToLive)
        if (result == null) {
            log.warn("Cache put feilet eller fikk timeout")
        } else if (result == "OK") {
            log.debug("Cache put OK type=${type.name}")
        }
    }

    override fun getAlleNavEnheter(): List<NavEnhet>? {
        val bytes: ByteArray? = redisStore.get(RedisKeyType.NORG_CLIENT.name + "_" + ALLE_NAVENHETER_CACHE_KEY)
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

    override fun get(type: RedisKeyType, key: String, requestedClass: Class<out Any>): Any? {
        return null
    }

    override fun set(type: RedisKeyType, key: String, value: ByteArray, timeToLive: Long) {
    }

    override fun getAlleNavEnheter(): List<NavEnhet>? {
        return null
    }
}
