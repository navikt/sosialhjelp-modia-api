package no.nav.sbl.sosialhjelpmodiaapi.redis

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.sosialhjelpmodiaapi.responses.ok_digisossak_response_string
import no.nav.sbl.sosialhjelpmodiaapi.responses.ok_kommuneinfo_response_string
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RedisServiceTest {

    private val redisStore: RedisStore = mockk()
    private val cacheProperties: CacheProperties = mockk(relaxed = true)

    private val service = RedisServiceImpl(redisStore, cacheProperties)

    @Test
    internal fun `skal hente fra store`() {
        every { redisStore.get(any()) } returns ok_digisossak_response_string.toByteArray()

        val digisosSak = service.get("key", DigisosSak::class.java)

        assertThat(digisosSak).isNotNull
    }

    @Test
    internal fun `store gir null`() {
        every { redisStore.get(any()) } returns null

        val digisosSak = service.get("key", DigisosSak::class.java)
        assertThat(digisosSak).isNull()
    }

    @Test
    internal fun `store gir feil type`() {
        every { redisStore.get(any()) } returns ok_kommuneinfo_response_string.toByteArray()

        val digisosSak = service.get("key", DigisosSak::class.java)
        assertThat(digisosSak).isNull()
    }
}