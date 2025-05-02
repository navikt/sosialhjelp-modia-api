package no.nav.sosialhjelp.modia.logging

import no.nav.sosialhjelp.modia.logging.cef.CommonEventFormat
import no.nav.sosialhjelp.modia.logging.cef.Extension
import no.nav.sosialhjelp.modia.logging.cef.Fiks
import no.nav.sosialhjelp.modia.logging.cef.Headers
import no.nav.sosialhjelp.modia.logging.cef.Severity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Component
class AuditLogger {
    fun report(values: Map<String, Any>) {
        val cef = createCef(values)
        if (cef.headers.severity == Severity.WARN) {
            sporingslogg.warn(cef.toString())
        } else {
            sporingslogg.info(cef.toString())
        }
    }

    private fun createCef(values: Map<String, Any>): CommonEventFormat {
        val extension =
            Extension(
                navIdent = values[NAVIDENT] as String,
                brukerFnr = values[BRUKER_FNR] as String,
                callId = values[CALL_ID] as String,
                consumerId = values[CONSUMER_ID] as String,
                url = values[URL] as String,
                httpMethod = values[HTTP_METHOD] as HttpMethod,
                fiks = populateFiksIfPresent(values),
            )

        return CommonEventFormat(
            headers =
                Headers(
                    log = SPORINGSLOGG,
                    resource = values.getOrDefault(RESOURCE, "") as String,
                    title = values.getOrDefault(TITLE, "") as String,
                    severity = getSeverity(values),
                ),
            extension = extension,
        )
    }

    private fun populateFiksIfPresent(values: Map<String, Any>): Fiks? =
        if (values.containsKey(FIKS_REQUEST_ID)) {
            Fiks(values[FIKS_REQUEST_ID] as String)
        } else {
            null
        }

    private fun getSeverity(values: Map<String, Any>): Severity = (values[SEVERITY] ?: Severity.INFO) as Severity

    companion object {
        // TODO: burde være private, men er public for å få testet klassen (https://github.com/mockk/mockk/issues/147)
        val sporingslogg: Logger = LoggerFactory.getLogger("sporingslogg")
    }
}
