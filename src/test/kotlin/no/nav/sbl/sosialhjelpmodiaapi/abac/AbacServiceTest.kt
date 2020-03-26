package no.nav.sbl.sosialhjelpmodiaapi.abac

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import no.nav.abac.xacml.NavAttributter
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.BEARER
import no.nav.sbl.sosialhjelpmodiaapi.utils.MiljoUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AbacServiceTest {

    private val abacClient: AbacClient = mockk()
    private val service = AbacService(abacClient)

    private val fnr = "fnr"

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        mockkObject(MiljoUtils)

        every { MiljoUtils.isProfileMockOrLocal() } returns false
    }

    @AfterEach
    internal fun tearDown() {
        unmockkAll()
    }

    @Test
    internal fun `harTilgang - profile mock eller local returnerer true`() {
        every { MiljoUtils.isProfileMockOrLocal() } returns true

        val tilgang = service.harTilgang(fnr, "token")

        verify(exactly = 0) { abacClient.sjekkTilgang(any()) }
        assertThat(tilgang).isTrue()
    }

    @Test
    internal fun `harTilgang - abacClient gir Permit, skal returnerer true`() {
        every { abacClient.sjekkTilgang(any()) } returns AbacResponse(Decision.Permit, null)

        val tilgang = service.harTilgang(fnr, "token")

        assertThat(tilgang).isTrue()
    }

    @Test
    internal fun `harTilgang - abacClient gir Deny, skal returnerer false`() {
        every { abacClient.sjekkTilgang(any()) } returns AbacResponse(Decision.Deny, null)

        val tilgang = service.harTilgang(fnr, "token")

        assertThat(tilgang).isFalse()
    }

    @Test
    internal fun `harTilgang - abacClient gir NotApplicable, skal returnerer false`() {
        every { abacClient.sjekkTilgang(any()) } returns AbacResponse(Decision.NotApplicable, null)

        val tilgang = service.harTilgang(fnr, "token")

        assertThat(tilgang).isFalse()
    }

    @Test
    internal fun `harTilgang - abacClient gir Indeterminate, skal returnerer false`() {
        every { abacClient.sjekkTilgang(any()) } returns AbacResponse(Decision.Indeterminate, null)

        val tilgang = service.harTilgang(fnr, "token")

        assertThat(tilgang).isFalse()
    }

    @Test
    internal fun `harTilgang - skal strippe bearer-prefiks fra token`() {
        val request = slot<Request>()

        every { abacClient.sjekkTilgang(capture(request)) } returns AbacResponse(Decision.Permit, null)

        val tilgang = service.harTilgang(fnr, "$BEARER part1.part2.part3")

        assertThat(tilgang).isTrue()

        assertThat(request.isCaptured).isTrue()
        assertThat(request.captured.environment?.attributes?.first { it.attributeId == NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY }?.value)
                .doesNotContain(BEARER)
                .isEqualTo("part2")
    }
}