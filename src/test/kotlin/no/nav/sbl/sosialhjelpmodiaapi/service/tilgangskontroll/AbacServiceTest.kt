package no.nav.sbl.sosialhjelpmodiaapi.service.tilgangskontroll

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import no.nav.abac.xacml.NavAttributter
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacClient
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacResponse
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.Decision
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.Request
import no.nav.sbl.sosialhjelpmodiaapi.common.TilgangskontrollException
import no.nav.sbl.sosialhjelpmodiaapi.utils.IntegrationUtils.BEARER
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
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
    }

    @AfterEach
    internal fun tearDown() {
        unmockkAll()
    }

    @Test
    internal fun `harTilgang - abacClient gir Permit - kaster ingen exception`() {
        every { abacClient.sjekkTilgang(any()) } returns AbacResponse(Decision.Permit, null)

        assertThatCode{ service.harTilgang(fnr, "token") }
                .doesNotThrowAnyException()
    }

    @Test
    internal fun `harTilgang - abacClient gir Deny - kaster TilgangskontrollException`() {
        every { abacClient.sjekkTilgang(any()) } returns AbacResponse(Decision.Deny, null)

        assertThatThrownBy { service.harTilgang(fnr, "token") }
                .isInstanceOf(TilgangskontrollException::class.java)
                .hasMessageContaining("${Decision.Deny}")
    }

    @Test
    internal fun `harTilgang - abacClient gir NotApplicable - kaster TilgangskontrollException`() {
        every { abacClient.sjekkTilgang(any()) } returns AbacResponse(Decision.NotApplicable, null)

        assertThatThrownBy { service.harTilgang(fnr, "token") }
                .isInstanceOf(TilgangskontrollException::class.java)
                .hasMessageContaining("${Decision.NotApplicable}")
    }

    @Test
    internal fun `harTilgang - abacClient gir Indeterminate - kaster TilgangskontrollException`() {
        every { abacClient.sjekkTilgang(any()) } returns AbacResponse(Decision.Indeterminate, null)

        assertThatThrownBy { service.harTilgang(fnr, "token") }
                .isInstanceOf(TilgangskontrollException::class.java)
                .hasMessageContaining("${Decision.Indeterminate}")
    }

    @Test
    internal fun `harTilgang - skal strippe bearer-prefiks fra token`() {
        val request = slot<Request>()

        every { abacClient.sjekkTilgang(capture(request)) } returns AbacResponse(Decision.Permit, null)

        service.harTilgang(fnr, "$BEARER part1.part2.part3")

        assertThat(request.isCaptured).isTrue()
        assertThat(request.captured.environment?.attributes?.first { it.attributeId == NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY }?.value)
                .doesNotContain(BEARER)
                .isEqualTo("part2")
    }
}