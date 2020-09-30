package no.nav.sbl.sosialhjelpmodiaapi.service.tilgangskontroll

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import no.nav.abac.xacml.NavAttributter
import no.nav.abac.xacml.NavAttributter.ADVICEOROBLIGATION_DENY_POLICY
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacClient
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacConstants.DENY_REASON
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacConstants.FP1_KODE6
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacConstants.FP2_KODE7
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacConstants.FP3_EGEN_ANSATT
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacConstants.SOSIALHJELP_AD_ROLLE
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.AbacResponse
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.Advice
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.Attribute
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.Decision
import no.nav.sbl.sosialhjelpmodiaapi.client.abac.Request
import no.nav.sbl.sosialhjelpmodiaapi.common.AbacException
import no.nav.sbl.sosialhjelpmodiaapi.common.ManglendeModiaSosialhjelpTilgangException
import no.nav.sbl.sosialhjelpmodiaapi.common.ManglendeTilgangException
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

    private val sosialhjelp = Attribute(ADVICEOROBLIGATION_DENY_POLICY, SOSIALHJELP_AD_ROLLE)
    private val kode6 = Attribute(ADVICEOROBLIGATION_DENY_POLICY, FP1_KODE6)
    private val kode7 = Attribute(ADVICEOROBLIGATION_DENY_POLICY, FP2_KODE7)
    private val egenAnsatt = Attribute(ADVICEOROBLIGATION_DENY_POLICY, FP3_EGEN_ANSATT)

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
    internal fun `deny uten advices - kaster AbacException`() {
        every { abacClient.sjekkTilgang(any()) } returns AbacResponse(Decision.Deny, null)

        assertThatThrownBy { service.harTilgang(fnr, "token") }
                .isInstanceOf(AbacException::class.java)
                .hasMessageContaining("${Decision.Deny}")
    }

    @Test
    internal fun `deny uten attributes - kaster AbacException`() {
        every { abacClient.sjekkTilgang(any()) } returns AbacResponse(Decision.Deny, listOf(Advice("id", null)))

        assertThatThrownBy { service.harTilgang(fnr, "token") }
                .isInstanceOf(AbacException::class.java)
                .hasMessageContaining("${Decision.Deny}")
    }

    @Test
    internal fun `notApplicable - kaster AbacException`() {
        every { abacClient.sjekkTilgang(any()) } returns AbacResponse(Decision.NotApplicable, null)

        assertThatThrownBy { service.harTilgang(fnr, "token") }
                .isInstanceOf(AbacException::class.java)
                .hasMessageContaining("${Decision.NotApplicable}")
    }

    @Test
    internal fun `indeterminate - kaster AbacException`() {
        every { abacClient.sjekkTilgang(any()) } returns AbacResponse(Decision.Indeterminate, null)

        assertThatThrownBy { service.harTilgang(fnr, "token") }
                .isInstanceOf(AbacException::class.java)
                .hasMessageContaining("${Decision.Indeterminate}")
    }

    @Test
    internal fun `skal strippe bearer-prefiks fra token`() {
        val request = slot<Request>()

        every { abacClient.sjekkTilgang(capture(request)) } returns AbacResponse(Decision.Permit, null)

        service.harTilgang(fnr, "$BEARER part1.part2.part3")

        assertThat(request.isCaptured).isTrue()
        assertThat(request.captured.environment?.attributes?.first { it.attributeId.endsWith(".felles.azure_jwt_token_body") }?.value)
                .doesNotContain(BEARER)
                .isEqualTo("part2")
    }

    @Test
    internal fun `deny - manglende tilgang til sosialhjelp`() {
        every { abacClient.sjekkTilgang(any()) } returns AbacResponse(Decision.Deny, listOf(Advice(DENY_REASON, listOf(sosialhjelp))))

        assertThatThrownBy { service.harTilgang(fnr, "token") }
                .isInstanceOf(ManglendeModiaSosialhjelpTilgangException::class.java)
                .hasMessageContaining("deny")
    }

    @Test
    internal fun `deny - manglende tilgang til kode6`() {
        every { abacClient.sjekkTilgang(any()) } returns AbacResponse(Decision.Deny, listOf(Advice(DENY_REASON, listOf(kode6))))

        assertThatThrownBy { service.harTilgang(fnr, "token") }
                .isInstanceOf(ManglendeTilgangException::class.java)
                .hasMessageContaining("deny")
    }

    @Test
    internal fun `deny - manglende tilgang til kode7`() {
        every { abacClient.sjekkTilgang(any()) } returns AbacResponse(Decision.Deny, listOf(Advice(DENY_REASON, listOf(kode7))))

        assertThatThrownBy { service.harTilgang(fnr, "token") }
                .isInstanceOf(ManglendeTilgangException::class.java)
                .hasMessageContaining("deny")
    }

    @Test
    internal fun `deny - manglende tilgang til egenAnsatt`() {
        every { abacClient.sjekkTilgang(any()) } returns AbacResponse(Decision.Deny, listOf(Advice(DENY_REASON, listOf(egenAnsatt))))

        assertThatThrownBy { service.harTilgang(fnr, "token") }
                .isInstanceOf(ManglendeTilgangException::class.java)
                .hasMessageContaining("deny")
    }
}