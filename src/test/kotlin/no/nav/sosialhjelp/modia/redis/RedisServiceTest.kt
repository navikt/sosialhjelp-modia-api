package no.nav.sosialhjelp.modia.redis

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.modia.responses.ok_digisossak_response_string
import no.nav.sosialhjelp.modia.responses.ok_kommuneinfo_response_string
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RedisServiceTest {

    private val cacheTimeToLiveSeconds = 1L
    private val redisStore: RedisStore = mockk()

    private val service = RedisServiceImpl(cacheTimeToLiveSeconds, redisStore)

    @Test
    internal fun `skal hente fra store`() {
        every { redisStore.get(any()) } returns ok_digisossak_response_string().toByteArray()

        val digisosSak = service.get(RedisKeyType.FIKS_CLIENT, "key", DigisosSak::class.java)

        assertThat(digisosSak).isNotNull
    }

    @Test
    internal fun `skal hente string-value fra store`() {
        every { redisStore.get(any()) } returns "tralala".toByteArray()

        val result = service.getString(RedisKeyType.AZUREDINGS, "key")

        assertThat(result).isEqualTo("tralala")
    }

    @Test
    internal fun `store gir null`() {
        every { redisStore.get(any()) } returns null

        val digisosSak = service.get(RedisKeyType.FIKS_CLIENT, "key", DigisosSak::class.java)
        assertThat(digisosSak).isNull()
    }

    @Test
    internal fun `store gir feil type`() {
        every { redisStore.get(any()) } returns ok_kommuneinfo_response_string.toByteArray()

        val digisosSak = service.get(RedisKeyType.SKJERMEDE_PERSONER, "key", DigisosSak::class.java)
        assertThat(digisosSak).isNull()
    }
}
