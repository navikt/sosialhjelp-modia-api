package no.nav.sosialhjelp.modia.logging

import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.slot
import no.nav.sosialhjelp.modia.logging.AuditLogger.Companion.sporingslogg
import no.nav.sosialhjelp.modia.logging.cef.Severity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod

internal class AuditLoggerTest {

    private val cefString = slot<String>()

    private val logger = AuditLogger()

    private val values = mutableMapOf<String, Any>(
        TITLE to "title",
        RESOURCE to RESOURCE_AUDIT_ACCESS,
        NAVIDENT to "Z999888",
        BRUKER_FNR to "11111122222",
        CALL_ID to "callid",
        CONSUMER_ID to "consumerId",
        URL to "http://test",
        HTTP_METHOD to HttpMethod.GET,
        SEVERITY to Severity.INFO,
        LOG to SPORINGSLOGG
    )

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        // FIXME: kan ikke static-mockke LoggerFactory https://github.com/mockk/mockk/issues/147
        mockkObject(sporingslogg)
        every { sporingslogg.info(capture(cefString)) } just Runs
    }

    @Test
    internal fun `should info log`() {
        logger.report(values)

        assertThat(cefString.isCaptured).isTrue
        assertThat(cefString.captured)
            // headers
            .contains("CEF:0|sosialhjelp-modia-api|$SPORINGSLOGG|1.0|$RESOURCE_AUDIT_ACCESS|title|INFO|")
            // extension
            .contains("suid=Z999888")
            .contains("duid=11111122222")
    }

    @Test
    internal fun `should info log fiks request`() {
        values[FIKS_REQUEST_ID] = "123123"

        logger.report(values)

        assertThat(cefString.isCaptured).isTrue
        assertThat(cefString.captured)
            // headers
            .contains("CEF:0|sosialhjelp-modia-api|$SPORINGSLOGG|1.0|$RESOURCE_AUDIT_ACCESS|title|INFO|")
            // extension
            .contains("suid=Z999888")
            .contains("duid=11111122222")
            .contains("cs5=123123 cs5Label=fiksRequestId")
    }
}
