package no.nav.sbl.sosialhjelpmodiaapi.logging

import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.slot
import no.nav.abac.xacml.NavAttributter
import no.nav.sbl.sosialhjelpmodiaapi.abac.AbacResponse
import no.nav.sbl.sosialhjelpmodiaapi.abac.Advice
import no.nav.sbl.sosialhjelpmodiaapi.abac.Attribute
import no.nav.sbl.sosialhjelpmodiaapi.abac.Decision
import no.nav.sbl.sosialhjelpmodiaapi.logging.AuditLogger.Companion.auditlog
import no.nav.sbl.sosialhjelpmodiaapi.logging.cef.Log
import no.nav.sbl.sosialhjelpmodiaapi.logging.cef.Severity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod

internal class AuditLoggerTest {

    private val cefString = slot<String>()

    private val logger = AuditLogger()

    private val values = mutableMapOf<String, Any>(
            TITLE to "title",
            RESOURCE to "resource",
            NAVIDENT to "Z999888",
            BRUKER_FNR to "11111122222",
            CALL_ID to "callid",
            CONSUMER_ID to "consumerId",
            URL to "http://test",
            HTTP_METHOD to HttpMethod.GET,
            SEVERITY to Severity.INFO,
            LOG to Log.ABAC
    )

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        // FIXME: kan ikke static-mockke LoggerFactory https://github.com/mockk/mockk/issues/147
        mockkObject(auditlog)
        every { auditlog.info(capture(cefString)) } just Runs
    }

    @Test
    internal fun `should info log`() {
        logger.report(values)

        assertThat(cefString.isCaptured).isTrue()
        assertThat(cefString.captured)
                // headers
                .contains("CEF:0|sosialhjelp-modia-api|ABAC|1.0|resource|title|INFO|")
                // extension
                .contains("suid=Z999888")
                .contains("duid=11111122222")
    }

    @Test
    internal fun `should warn log abac deny`() {
        every { auditlog.warn(capture(cefString)) } just Runs

        values[SEVERITY] = Severity.WARN
        values[ABAC_RESPONSE] = createAbacDeny()

        logger.report(values)

        assertThat(cefString.isCaptured).isTrue()
        assertThat(cefString.captured)
                // headers
                .contains("CEF:0|sosialhjelp-modia-api|ABAC|1.0|resource|title|WARN|")
                // extension
                .contains("suid=Z999888")
                .contains("duid=11111122222")
                .contains("flexString1=Deny flexString1Label=Decision")
                .contains("flexString2=1_denyPolicy,2_denyPolicy flexString2Label=deny_policy")
                .contains("cs3=1_cause,2_cause cs3Label=deny_cause")
    }

    @Test
    internal fun `should info log fiks request`() {
        values[LOG] = Log.AUDIT
        values[FIKS_REQUEST_ID] = "123123"

        logger.report(values)

        assertThat(cefString.isCaptured).isTrue()
        assertThat(cefString.captured)
                // headers
                .contains("CEF:0|sosialhjelp-modia-api|AUDIT|1.0|resource|title|INFO|")
                // extension
                .contains("suid=Z999888")
                .contains("duid=11111122222")
                .contains("cs5=123123 cs5Label=fiksRequestId")
    }

    private fun createAbacDeny(): AbacResponse {
        return AbacResponse(Decision.Deny, listOf(
                Advice("id", listOf(
                        Attribute(NavAttributter.ADVICEOROBLIGATION_DENY_POLICY, "1_denyPolicy"),
                        Attribute(NavAttributter.ADVICEOROBLIGATION_CAUSE, "1_cause"))
                ),
                Advice("id2", listOf(
                        Attribute(NavAttributter.ADVICEOROBLIGATION_DENY_POLICY, "2_denyPolicy"),
                        Attribute(NavAttributter.ADVICEOROBLIGATION_CAUSE, "2_cause"))
                )
        ))

    }
}