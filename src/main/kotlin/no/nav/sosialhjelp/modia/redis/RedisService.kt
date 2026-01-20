package no.nav.sosialhjelp.modia.redis

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.modia.app.exceptions.DigisosSakTilhorerAnnenBrukerException
import no.nav.sosialhjelp.modia.logger
import no.nav.sosialhjelp.modia.navkontor.norg.NavEnhet
import no.nav.sosialhjelp.modia.utils.sosialhjelpJsonMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import tools.jackson.module.kotlin.KotlinInvalidNullException
import tools.jackson.module.kotlin.readValue
import java.io.IOException
import java.nio.charset.StandardCharsets

enum class RedisKeyType {
    AZUREDINGS,
    SKJERMEDE_PERSONER,
    FIKS_CLIENT,
    NORG_CLIENT,
    KOMMUNE_SERVICE,
    FNR_SERVICE,
}

interface RedisService {
    val defaultTimeToLiveSeconds: Long

    fun <T : Any> get(
        type: RedisKeyType,
        key: String,
        requestedClass: Class<out T>,
    ): T?

    fun getString(
        type: RedisKeyType,
        key: String,
    ): String?

    fun set(
        type: RedisKeyType,
        key: String,
        value: ByteArray,
        timeToLive: Long = defaultTimeToLiveSeconds,
    )

    fun getAlleNavEnheter(): List<NavEnhet>?
}

@Profile("!no-redis")
@Component
class RedisServiceImpl(
    @Value("\${cache_time_to_live_seconds}") private val cacheTimeToLiveSeconds: Long,
    private val redisStore: RedisStore,
) : RedisService {
    override val defaultTimeToLiveSeconds = cacheTimeToLiveSeconds

    override fun <T : Any> get(
        type: RedisKeyType,
        key: String,
        requestedClass: Class<out T>,
    ): T? {
        val bytes: ByteArray = getBytes(type, key) ?: return null
        return try {
            sosialhjelpJsonMapper
                .readValue(bytes, requestedClass)
                .also { valider(it) }
                .also { log.debug("Hentet ${requestedClass.simpleName} fra cache, type=${type.name}") } as T
        } catch (ignored: KotlinInvalidNullException) {
            log.warn("Fant type=${type.name} i cache, men value var ikke ${requestedClass.simpleName}")
            null
        } catch (ignored: DigisosSakTilhorerAnnenBrukerException) {
            log.warn("DigisosSak i cache tilhører en annen bruker enn brukeren fra token.")
            null
        }
    }

    override fun getString(
        type: RedisKeyType,
        key: String,
    ): String? {
        val bytes: ByteArray = getBytes(type, key) ?: return null
        return try {
            log.debug("Hentet String fra cache, type=${type.name}")
            String(bytes, StandardCharsets.UTF_8)
        } catch (ignored: IOException) {
            log.warn("Fant type=${type.name} i cache, men value var ikke String")
            null
        }
    }

    override fun set(
        type: RedisKeyType,
        key: String,
        value: ByteArray,
        timeToLive: Long,
    ) {
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
                sosialhjelpJsonMapper.readValue(bytes)
            } catch (ignored: IOException) {
                log.warn("Fant key=$ALLE_NAVENHETER_CACHE_KEY, men feil oppstod ved deserialisering til List<NavEnhet>")
                null
            }
        } else {
            null
        }
    }

    private fun getBytes(
        type: RedisKeyType,
        key: String,
    ): ByteArray? {
        return redisStore.get("${type.name}_$key") // Redis har konfigurert timout for disse.
    }

    /**
     * Kaster feil hvis det finnes additionalProperties på mappet objekt.
     * Tyder på at noe feil har skjedd ved mapping.
     */
    private fun valider(obj: Any?) {
        when {
            obj is JsonDigisosSoker && obj.additionalProperties.isNotEmpty() -> throw IOException(
                "JsonDigisosSoker har ukjente properties - må tilhøre ett annet objekt. Cache-value tas ikke i bruk",
            )
            obj is JsonVedleggSpesifikasjon && obj.additionalProperties.isNotEmpty() -> throw IOException(
                "JsonVedleggSpesifikasjon har ukjente properties - må tilhøre ett annet objekt. Cache-value tas ikke i bruk",
            )
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

    override fun <T : Any> get(
        type: RedisKeyType,
        key: String,
        requestedClass: Class<out T>,
    ): T? = null

    override fun getString(
        type: RedisKeyType,
        key: String,
    ): String? = null

    override fun set(
        type: RedisKeyType,
        key: String,
        value: ByteArray,
        timeToLive: Long,
    ) {
    }

    override fun getAlleNavEnheter(): List<NavEnhet>? = null
}
